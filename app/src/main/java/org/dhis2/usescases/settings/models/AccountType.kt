package org.dhis2.usescases.settings.models

import org.hisp.dhis.android.core.common.AuthorizationType

enum class AccountType {
    BASIC,
    OAUTH,
    OPEN_ID,
}

fun AuthorizationType?.toAccountType(): AccountType =
    when (this) {
        AuthorizationType.OPEN_ID_CONNECT -> AccountType.OPEN_ID
        AuthorizationType.OAUTH2 -> AccountType.OAUTH
        else -> AccountType.BASIC
    }
