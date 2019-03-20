package org.dhis2.usescases.splash

import org.hisp.dhis.android.core.D2

/**
 * QUADRAM. Created by ppajuelo on 16/05/2018.
 */

class SplashRepositoryImpl internal constructor(private val d2: D2?) : SplashRepository {

    override fun iconForFlag() =
            if (d2 != null)
                d2.systemSettingModule().systemSetting.flag().get().value() ?: ""
            else
                ""

}

