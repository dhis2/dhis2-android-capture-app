package org.dhis2.mobile.login.main.domain.model

data class SessionRenewalRequest(
    val serverUrl: String,
    val isNetworkAvailable: Boolean,
)
