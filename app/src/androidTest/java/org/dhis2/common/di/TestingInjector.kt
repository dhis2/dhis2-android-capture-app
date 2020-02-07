package org.dhis2.common.di

import android.content.Context
import org.dhis2.common.KeyStoreRobot
import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore

class TestingInjector {

    companion object {
        fun createKeyStoreRobot(context: Context) : KeyStoreRobot{
            return KeyStoreRobot(AndroidSecureStore(context))
        }
    }
}