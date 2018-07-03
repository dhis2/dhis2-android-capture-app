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
            "SystemSetting.value FROM SystemSetting WHERE SystemSetting.key = 'flag'";

    private final String EXPIRED_EVENTS = "SELECT * FROM Event WHERE Event.dueDate IS NOT NULL";

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
