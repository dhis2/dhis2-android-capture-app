package com.dhis2.usescases.splash;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.settings.SystemSettingModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 16/05/2018.
 */

public class SplashRepositoryImpl implements SplashRepository {

    private final String FLAG_QUERY = "SELECT " +
            "SystemSetting.value FROM SystemSetting WHERE SystemSetting.key = 'flag' LIMIT 1";

    private final String EXPIRED_EVENTS = "SELECT * FROM Event WHERE Event.dueDate IS NOT NULL";

    private final String EXPIRED_PERIOD_EVENTS = String.format("SELECT %s.%s, %s.%s, %s.%s, %s.%s FROM Event " +
                    "JOIN Program ON Event.program = Program.uid WHERE Event.status = 'COMPLETED'",
            EventModel.TABLE, EventModel.Columns.COMPLETE_DATE, ProgramModel.TABLE, ProgramModel.Columns.EXPIRY_DAYS,
            ProgramModel.TABLE, ProgramModel.Columns.COMPLETE_EVENTS_EXPIRY_DAYS, ProgramModel.TABLE, ProgramModel.Columns.EXPIRY_PERIOD_TYPE);

    private final BriteDatabase briteDatabase;

    SplashRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<String> getIconForFlag() {
        return briteDatabase.createQuery(SystemSettingModel.TABLE, FLAG_QUERY)
                .mapToOne(cursor -> cursor.getString(0));
    }

    @Override
    public Observable<Boolean> checkExpiredEvents() {
        Date today = Calendar.getInstance().getTime();
        return briteDatabase.createQuery(EventModel.TABLE, EXPIRED_EVENTS)
                .mapToList(EventModel::create)
                .map(eventModels -> Observable.fromIterable(eventModels)
                        .filter(eventModel -> eventModel.dueDate().before(today))
                        .map(eventModel -> switchToExpired(eventModel))
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
                EventModel.Columns.UID + " = ?", eventModel.uid()) <= 0) {

            throw new IllegalStateException(String.format(Locale.US, "Event=[%s] " +
                    "has not been successfully updated", eventModel.uid()));
        }

        return eventModel;
    }


}
