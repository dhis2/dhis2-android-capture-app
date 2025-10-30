package org.dhis2.common.di

import android.content.Context
import org.dhis2.DBTestLoader
import org.dhis2.common.FileReader
import org.dhis2.common.featureConfig.FeatureConfigRobot
import org.dhis2.common.keystore.KeyStoreRobot
import org.dhis2.common.mockwebserver.MockWebServerRobot
import org.dhis2.common.preferences.PreferenceTestingImpl
import org.dhis2.common.preferences.PreferencesRobot
import org.dhis2.commons.featureconfig.data.FeatureConfigRepositoryImpl
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore
import org.hisp.dhis.android.core.mockwebserver.Dhis2MockServer

class TestingInjector {

    companion object {
        private const val CONFIG_FILE = "smsconfig"

        private var keystore: AndroidSecureStore? = null

        fun providesKeyStoreRobot(context: Context): KeyStoreRobot {
            keystore = AndroidSecureStore(context)
            return KeyStoreRobot(AndroidSecureStore(context))
        }

        fun providesPreferencesRobot(context: Context): PreferencesRobot {
            return PreferencesRobot(
                PreferenceTestingImpl(context),
                context.getSharedPreferences(
                    CONFIG_FILE,
                    Context.MODE_PRIVATE
                )
            )
        }

        fun providesFeatureConfigRobot(): FeatureConfigRobot {
            return FeatureConfigRobot(FeatureConfigRepositoryImpl(D2Manager.getD2()))
        }

        fun providesMockWebserverRobot(testContext: Context): MockWebServerRobot {
            return MockWebServerRobot(Dhis2MockServer(FileReader(testContext), 8080))
        }

        fun provideDBImporter(context: Context): DBTestLoader {
            return DBTestLoader(context)
        }

        fun getStorage(): AndroidSecureStore {
            return keystore!!
        }
    }
}
