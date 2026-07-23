package org.dhis2.usescases.main.domain.model

sealed interface LogoutAction {
    data object CreatePin : LogoutAction

    data class SuccessLogout(
        val accountCount: Int,
    ) : LogoutAction
}
