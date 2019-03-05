package org.dhis2.usescases.splash;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;

import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.SYSTEM_SETTING_TABLE;

/**
 * QUADRAM. Created by ppajuelo on 16/05/2018.
 */

public class SplashRepositoryImpl implements SplashRepository {

    private static final String FLAG_QUERY = "SELECT " +
            "SystemSetting.VALUE FROM SystemSetting WHERE SystemSetting.key = 'flag' LIMIT 1";

    private static final String EXPIRED_EVENTS = "SELECT * FROM Event WHERE Event.dueDate IS NOT NULL";

    private final BriteDatabase briteDatabase;

    SplashRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<String> getIconForFlag() {
        return briteDatabase.createQuery(SYSTEM_SETTING_TABLE, FLAG_QUERY)
                .mapToOne(cursor -> cursor.getString(0));
    }

    @Override
    public Observable<Boolean> checkExpiredEvents() {
        Date today = Calendar.getInstance().getTime();
        return briteDatabase.createQuery(EventModel.TABLE, EXPIRED_EVENTS)
                .mapToList(EventModel::create)
                .map(eventModels -> Observable.fromIterable(eventModels)
                        .filter(eventModel -> eventModel.dueDate().before(today))
                        .map(this::switchToExpired)
                        .toList()
                )
                /*.flatMap(eventModels -> briteDatabase.createQuery(EventModel.TABLE, EXPIRED_PERIOD_EVENTS)
                        .mapToList(cursor -> Quartet.create(cursor.getString(0),
                                cursor.getInt(1),
                                cursor.getInt(2),
                                cursor.getString(3)))
                        .map(eventModels1 -> Observable.fromIterable(eventModels1)
                                .filter(completedEvent ->
                                        DateUtils.getInstance().hasExpired(
                                                DateUtils.databaseDateFormat().parse(completedEvent.val0()),
                                                completedEvent.val1(),
                                                completedEvent.val2(),
                                                PeriodType.valueOf(completedEvent.val3())))
                                .map(completedEvent-> switchToExpired(completedEvent))
                                .toList()
                        )
                        )*/
                .flatMap(eventModels -> Observable.just(true));
    }

    private EventModel switchToExpired(EventModel eventModel) {

        ContentValues contentValues = eventModel.toContentValues();
        contentValues.put(EventModel.Columns.STATUS, EventStatus.SKIPPED.name());

        if (briteDatabase.update(EventModel.TABLE, contentValues,
                EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventModel.uid()) <= 0) {

            throw new IllegalStateException(String.format(Locale.US, "Event=[%s] " +
                    "has not been successfully updated", eventModel.uid()));
        }

        return eventModel;
    }


}
