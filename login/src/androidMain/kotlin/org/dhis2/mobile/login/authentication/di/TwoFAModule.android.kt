package org.dhis2.mobile.login.authentication.di

import android.content.ClipboardManager
import android.content.Context
import org.dhis2.mobile.login.authentication.SensitiveClipboard
import org.dhis2.mobile.login.authentication.data.repository.TwoFARepositoryImpl
import org.dhis2.mobile.login.authentication.domain.repository.SecretClipboard
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val twoFARepositoryModule: Module =
    module {
        factory<TwoFARepository> {
            TwoFARepositoryImpl(
                get(),
                get(),
            )
        }
        factory<SecretClipboard> {
            SensitiveClipboard(
                androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
            )
        }
    }
