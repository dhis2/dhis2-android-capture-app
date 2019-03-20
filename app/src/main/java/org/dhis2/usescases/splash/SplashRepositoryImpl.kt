package org.dhis2.usescases.splash

import com.squareup.sqlbrite2.BriteDatabase
import org.hisp.dhis.android.core.settings.SystemSettingModel

/**
 * QUADRAM. Created by ppajuelo on 16/05/2018.
 */

class SplashRepositoryImpl internal constructor(private val briteDatabase: BriteDatabase) : SplashRepository {

    companion object {

        const val FLAG_QUERY = "SELECT " + "SystemSetting.value FROM SystemSetting WHERE SystemSetting.key = 'flag' LIMIT 1"
    }

    override fun iconForFlag() = briteDatabase.createQuery(SystemSettingModel.TABLE, FLAG_QUERY)
            .mapToOneOrDefault({ cursor -> cursor.getString(0) }, "")
}

