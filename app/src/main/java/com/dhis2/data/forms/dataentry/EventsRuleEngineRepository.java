package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.FormRepository;
import com.dhis2.utils.Result;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Flowable;

public final class EventsRuleEngineRepository implements RuleEngineRepository {
    private static final String QUERY_EVENT = "SELECT uid,\n" +
            "  programStage,\n" +
            "  status,\n" +
            "  eventDate,\n" +
            "  dueDate\n" +
            "FROM Event\n" +
            "WHERE uid = ?\n" +
            "LIMIT 1;";

    private static final String QUERY_VALUES = "SELECT " +
            "  eventDate," +
            "  programStage," +
            "  dataElement," +
            "  value" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            " WHERE event = ? AND value IS NOT NULL;";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final String eventUid;

    EventsRuleEngineRepository(@NonNull BriteDatabase briteDatabase,
                               @NonNull FormRepository formRepository, @NonNull String eventUid) {
        this.briteDatabase = briteDatabase;
        this.formRepository = formRepository;
        this.eventUid = eventUid;
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues()
                .switchMap(this::queryEvent)
                .switchMap(event -> formRepository.ruleEngine()
                        .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(event))
                                .map(Result::success)
                                .onErrorReturn(error -> Result.failure(new Exception(error)))
                        )
                );
    }

    @NonNull
    private Flowable<RuleEvent> queryEvent(@NonNull List<RuleDataValue> dataValues) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENT, eventUid)
                .mapToOne(cursor -> {
                    Date eventDate = parseDate(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : parseDate(cursor.getString(4));

                    RuleEvent.Status status = RuleEvent.Status.valueOf(cursor.getString(2));
                    return RuleEvent.create(cursor.getString(0), cursor.getString(1),
                            status, eventDate, dueDate, dataValues);
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues() {
        return briteDatabase.createQuery(Arrays.asList(EventModel.TABLE,
                TrackedEntityDataValueModel.TABLE), QUERY_VALUES, eventUid)
                .mapToList(cursor -> {
                    Date eventDate = parseDate(cursor.getString(0));
                    return RuleDataValue.create(eventDate, cursor.getString(1),
                            cursor.getString(2), cursor.getString(3));
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private static Date parseDate(@NonNull String date) {
        try {
            return BaseIdentifiableObject.DATE_FORMAT.parse(date);
        } catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }
}
