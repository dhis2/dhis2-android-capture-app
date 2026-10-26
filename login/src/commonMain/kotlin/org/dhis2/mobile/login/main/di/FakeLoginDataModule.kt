package org.dhis2.mobile.login.main.di

import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.data.repository.FakeAccountRepository
import org.dhis2.mobile.login.authentication.data.repository.FakeTwoFARepository
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.dhis2.mobile.login.main.data.FakeLoginRepository
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.ui.state.OidcInfo
import org.dhis2.mobile.login.pin.data.FakeSessionRepository
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.koin.dsl.module

internal val fakeAccountModule =
    module {
        factory<LoginRepository> { FakeLoginRepository() }
        factory<AccountRepository> { FakeAccountRepository() }
        single<OidcInfo> {
            OidcInfo.Discovery(
                server = "",
                loginButtonText = null,
                clientId = "",
                redirectUri = "",
                discoveryUri = "",
                prompt = null,
            )
        }
    }

internal val fakePinDataModule =
    module {
        single<SessionRepository> { FakeSessionRepository() }
    }

internal val fakeTwoFARepositoryModule =
    module {
        single<TwoFARepository> { FakeTwoFARepository() }
    }

/**
 * Login data layer backed by fakes, for targets without the DHIS2 Android SDK and for running the
 * login UI in isolation.
 */
val fakeLoginDataModule =
    module {
        includes(fakeAccountModule, fakePinDataModule, fakeTwoFARepositoryModule)
    }
