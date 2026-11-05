package org.dhis2.mobile.login.authentication.di

import org.dhis2.mobile.login.authentication.domain.repository.SecretClipboard
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val twoFARepositoryModule: Module
    get() =
        module {
            // 2FA settings are not available on Desktop yet.
            factory<SecretClipboard> {
                object : SecretClipboard {
                    override fun copy(text: String) = Unit

                    override fun clearIfOwned() = Unit
                }
            }
        }
