package org.dhis2.common.di

import android.content.Context
import org.dhis2.common.keystore.KeyStoreRobot
//import org.dhis2.common.mockwebserver.MockWebServerRobot
import org.dhis2.common.preferences.PreferenceTestingImpl
import org.dhis2.common.preferences.PreferencesRobot
import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore
//import org.hisp.dhis.android.core.data.server.Dhis2MockServer

class TestingInjector {

    companion object {
        fun providesKeyStoreRobot(context: Context) : KeyStoreRobot {
            return KeyStoreRobot(AndroidSecureStore(context))
        }
        fun providesPreferencesRobot(context:Context) : PreferencesRobot {
            return PreferencesRobot(PreferenceTestingImpl(context))
        }
    //    fun providesMockWebserverRobot() : MockWebServerRobot{
        //        return MockWebServerRobot(Dhis2MockServer(8080))
        //    }
    }
}