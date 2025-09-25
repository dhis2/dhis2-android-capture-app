package org.dhis2.mobile.login.accounts.data.repository

import org.dhis2.mobile.login.BuildConfig
import org.dhis2.mobile.login.accounts.data.credentials.defaultTestingCredentials
import org.dhis2.mobile.login.accounts.data.credentials.trainingTestingCredentials
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.hisp.dhis.android.core.D2

class AccountRepositoryImpl(
    private val d2: D2,
) : AccountRepository {
    override suspend fun getLoggedInAccounts(): List<AccountModel> =
        d2.userModule().accountManager().getAccounts().map {
            val oidcProviders = it.loginConfig()?.oidcProviders?.firstOrNull()
            AccountModel(
                name = it.username(),
                serverUrl = it.serverUrl(),
                serverName = it.loginConfig()?.applicationTitle,
                serverDescription = it.loginConfig()?.applicationDescription,
                allowRecovery = it.loginConfig()?.allowAccountRecovery == true,
                oidcIcon = oidcProviders?.icon,
                oidcLoginText = oidcProviders?.loginText,
                oidcUrl = oidcProviders?.url,
                isOauthEnabled = it.loginConfig()?.isOauthEnabled() == true,
            )
        }

    override suspend fun availableServers(): List<String> {
        val providedServers =
            if (BuildConfig.DEBUG) {
                defaultTestingCredentials
            } else if (BuildConfig.FLAVOR == "dhis2Training") {
                trainingTestingCredentials
            } else {
                emptyList()
            }
        return providedServers.map { it.server }
    }
}
