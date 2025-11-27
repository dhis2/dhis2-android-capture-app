package org.dhis2.mobile.login.main.ui.state

internal sealed interface CredentialsAction {
    data object OnLoginClicked : CredentialsAction

    data object OnOpenIdLogin : CredentialsAction

    data object OnBiometricsClicked : CredentialsAction

    data object OnManageAccounts : CredentialsAction

    data object OnRecoverAccount : CredentialsAction
}
