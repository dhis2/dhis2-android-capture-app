package org.dhis2.mobile.login.main.domain.model

import org.dhis2.mobile.login.accounts.domain.model.AuthorizationMethod

enum class CredentialsEntryMode {
    NEW_ACCOUNT_BASIC,
    NEW_ACCOUNT_OAUTH,
    EXISTING_BASIC,
    EXISTING_OPEN_ID,
    EXISTING_OAUTH,
    ;

    companion object {
        fun existing(method: AuthorizationMethod): CredentialsEntryMode =
            when (method) {
                AuthorizationMethod.BASIC -> EXISTING_BASIC
                AuthorizationMethod.OPEN_ID_CONNECT -> EXISTING_OPEN_ID
                AuthorizationMethod.OAUTH2 -> EXISTING_OAUTH
            }
    }
}
