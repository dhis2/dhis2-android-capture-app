package org.dhis2.usescases.splash

import com.squareup.sqlbrite2.BriteDatabase

import org.hisp.dhis.android.core.settings.SystemSetting
import org.hisp.dhis.android.core.settings.SystemSettingModel

import io.reactivex.Observable

/**
 * QUADRAM. Created by ppajuelo on 16/05/2018.
 */

class SplashRepositoryImpl internal constructor(private val briteDatabase: BriteDatabase) : SplashRepository {

    override val iconForFlag: Observable<String>
        get() = briteDatabase.createQuery(SystemSettingModel.TABLE, FLAG_QUERY)
                .mapToOneOrDefault({ cursor -> cursor.getString(0) }, "")

    companion object {

        private val FLAG_QUERY = "SELECT " + "SystemSetting.value FROM SystemSetting WHERE SystemSetting.key = 'flag' LIMIT 1"
    }

}
