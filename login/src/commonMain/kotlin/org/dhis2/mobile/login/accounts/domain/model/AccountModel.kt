package org.dhis2.mobile.login.accounts.domain.model

data class AccountModel(
    val name: String,
    val serverUrl: String,
    val serverName: String,
    val serverDescription: String?,
    var serverFlag: String?,
    val allowRecovery: Boolean,
    val oidcIcon: String?,
    val oidcLoginText: String?,
    val oidcUrl: String?,
    val isOauthEnabled: Boolean,
) {
    fun key() = "$name@$serverUrl"
}
