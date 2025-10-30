package org.dhis2.mobile.login.accounts.data.repository

import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.login.BuildConfig
import org.dhis2.mobile.login.accounts.data.credentials.defaultTestingCredentials
import org.dhis2.mobile.login.accounts.data.credentials.trainingTestingCredentials
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.data.PREF_URLS
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.configuration.internal.DatabaseAccount

class AccountRepositoryImpl(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
) : AccountRepository {
    override suspend fun getLoggedInAccounts(): List<AccountModel> =
        d2.userModule().accountManager().getAccounts().map {
            mapDatabaseAccountToAccountModel(it)
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

        providedServers.forEach {
            preferenceProvider.updateLoginServers(it.server)
        }

        return preferenceProvider
            .getSet(PREF_URLS, HashSet())
            .orEmpty()
            .toList()
    }

    override suspend fun getActiveAccount(): AccountModel? =
        d2.userModule().accountManager().getCurrentAccount()?.let {
            mapDatabaseAccountToAccountModel(it)
        }

    private fun mapDatabaseAccountToAccountModel(databaseAccount: DatabaseAccount): AccountModel {
        val oidcProviders = databaseAccount.loginConfig()?.oidcProviders?.firstOrNull()
        val serverName =
            databaseAccount.loginConfig()?.applicationTitle ?: try {
                databaseAccount.serverUrl().substringAfter("://").substringBefore("/")
            } catch (_: Exception) {
                databaseAccount.serverUrl()
            }
        return AccountModel(
            name = databaseAccount.username(),
            serverName = serverName,
            serverUrl = databaseAccount.serverUrl(),
            serverDescription = databaseAccount.loginConfig()?.applicationDescription,
            serverFlag = databaseAccount.loginConfig()?.countryFlag,
            allowRecovery = databaseAccount.loginConfig()?.allowAccountRecovery == true,
            oidcIcon = oidcProviders?.icon,
            oidcLoginText = oidcProviders?.loginText,
            oidcUrl = oidcProviders?.url,
            isOauthEnabled = databaseAccount.loginConfig()?.isOauthEnabled() == true,
        )
    }
}
