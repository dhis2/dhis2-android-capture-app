package org.dhis2.common.di

import android.content.Context
import org.dhis2.common.keystore.KeyStoreRobot
import org.dhis2.common.preferences.PreferenceTestingImpl
import org.dhis2.common.preferences.PreferencesRobot
import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore

class TestingInjector {

    companion object {
        fun providesKeyStoreRobot(context: Context) : KeyStoreRobot {
            return KeyStoreRobot(AndroidSecureStore(context))
        }
        fun providesPreferencesRobot(context:Context) : PreferencesRobot {
            return PreferencesRobot(PreferenceTestingImpl(context))
        }
    }
}