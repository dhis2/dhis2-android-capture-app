package org.dhis2.mobile.login.main.domain.model

import org.dhis2.mobile.login.accounts.domain.model.AuthorizationMethod

enum class CredentialsEntryMode {
    NEW_ACCOUNT_BASIC,
    NEW_ACCOUNT_OAUTH,
    EXISTING_PASSWORD,
    EXISTING_OPEN_ID,
    EXISTING_OAUTH,
    ;

    fun usesOfflineCredential(): Boolean = this == EXISTING_OAUTH || this == EXISTING_OPEN_ID

    companion object {
        fun existing(method: AuthorizationMethod): CredentialsEntryMode =
            when (method) {
                AuthorizationMethod.BASIC -> EXISTING_PASSWORD
                AuthorizationMethod.OPEN_ID_CONNECT -> EXISTING_OPEN_ID
                AuthorizationMethod.OAUTH2 -> EXISTING_OAUTH
            }
    }
}
