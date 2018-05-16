package com.dhis2.usescases.splash;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.settings.SystemSettingModel;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 16/05/2018.
 */

public class SplashRepositoryImpl implements SplashRepository {

    private final String FLAG_QUERY = "SELECT " +
            "SystemSetting.value FROM SystemSetting WHERE SystemSetting.key = 'flag'";

    private final BriteDatabase briteDatabase;

    SplashRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<String> getIconForFlag() {
        return briteDatabase.createQuery(SystemSettingModel.TABLE, FLAG_QUERY)
                .mapToOne(cursor -> cursor.getString(0));
    }
}
