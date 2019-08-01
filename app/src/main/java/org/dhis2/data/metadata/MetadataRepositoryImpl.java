package org.dhis2.data.metadata;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.option.OptionGroup;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;
import static android.text.TextUtils.join;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public class MetadataRepositoryImpl implements MetadataRepository {


    private static final String EXPIRY_DATE_PERIOD_QUERY = String.format(
            "SELECT program.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "LIMIT 1",
            ProgramModel.TABLE,
            EventModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID, EventModel.TABLE, EventModel.Columns.PROGRAM,
            EventModel.TABLE, EventModel.Columns.UID);

    private final BriteDatabase briteDatabase;

    MetadataRepositoryImpl(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @Override
    public Observable<ProgramModel> getExpiryDateFromEvent(String eventUid) {
        return briteDatabase
                .createQuery(ProgramModel.TABLE, EXPIRY_DATE_PERIOD_QUERY, eventUid == null ? "" : eventUid)
                .mapToOne(ProgramModel::create);
    }


    @Override
    public Flowable<Pair<Integer, Integer>> getDownloadedData() {
        String TEI_COUNT = "SELECT DISTINCT COUNT (uid) FROM TrackedEntityInstance WHERE TrackedEntityInstance.state != 'RELATIONSHIP'";
        String EVENT_COUNT = "SELECT DISTINCT COUNT (uid) FROM Event WHERE Event.enrollment IS NULL";

        int currentTei = 0;
        int currentEvent = 0;

        try (Cursor teiCursor = briteDatabase.query(TEI_COUNT)) {
            if (teiCursor != null && teiCursor.moveToFirst()) {
                currentTei = teiCursor.getInt(0);
            }
        }

        try (Cursor eventCursor = briteDatabase.query(EVENT_COUNT)) {
            if (eventCursor != null && eventCursor.moveToFirst()) {
                currentEvent = eventCursor.getInt(0);
            }
        }
        return Flowable.just(Pair.create(currentEvent, currentTei));
    }


    @Override
    public Observable<List<OptionModel>> searchOptions(String text, String idOptionSet, int page, List<String> optionsToHide, List<String> optionsGroupsToHide) {
        String pageQuery = String.format(Locale.US, "GROUP BY Option.uid ORDER BY sortOrder LIMIT %d,%d", page * 15, 15);
        String formattedOptionsToHide = "'" + join("','", optionsToHide) + "'";

        String optionQuery = "SELECT Option.* FROM Option WHERE Option.optionSet = ? " +
                (!optionsToHide.isEmpty() ? "AND Option.uid NOT IN (" + formattedOptionsToHide + ") " : " ") +
                (!isEmpty(text) ? "AND Option.displayName LIKE '%" + text + "%' " : " ") +
                pageQuery;

        return briteDatabase.createQuery(OptionModel.TABLE, optionQuery, idOptionSet)
                .mapToList(OptionModel::create)
                .map(optionList -> {
                    Iterator<OptionModel> iterator = optionList.iterator();
                    while (iterator.hasNext()) {
                        OptionModel option = iterator.next();
                        List<String> optionGroupUids = new ArrayList<>();
                        try (Cursor optionGroupCursor = briteDatabase.query("SELECT OptionGroup.* FROM OptionGroup " +
                                "LEFT JOIN OptionGroupOptionLink ON OptionGroupOptionLink.optionGroup = OptionGroup.uid WHERE OptionGroupOptionLink.option = ?", option.uid())) {
                            if (optionGroupCursor.moveToFirst()) {
                                for (int i = 0; i < optionGroupCursor.getCount(); i++) {
                                    optionGroupUids.add(OptionGroup.create(optionGroupCursor).uid());
                                    optionGroupCursor.moveToNext();
                                }
                            }
                        }
                        boolean remove = false;
                        for (String group : optionGroupUids)
                            if (optionsGroupsToHide.contains(group))
                                remove = true;

                        if (remove)
                            iterator.remove();

                    }
                    return optionList;
                });
    }
}