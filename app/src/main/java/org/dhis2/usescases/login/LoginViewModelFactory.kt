package org.dhis2.usescases.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.server.UserManager

class LoginViewModelFactory(
    private val view: LoginContracts.View,
    private val resourceManager: ResourceManager,
    private val userManager: UserManager?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(
            view,
            resourceManager,
            userManager,
        ) as T
}
