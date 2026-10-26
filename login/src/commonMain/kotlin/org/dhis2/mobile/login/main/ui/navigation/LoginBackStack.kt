package org.dhis2.mobile.login.main.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.dhis2.mobile.login.main.domain.model.LoginScreenState

/**
 * Lets the login back stack be saved on targets without reflection (iOS), where every [NavKey] must
 * be registered for polymorphic serialization.
 */
internal val loginSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(LoginScreenState.Loading::class)
                    subclass(LoginScreenState.ServerValidation::class)
                    subclass(LoginScreenState.LoginCredentials::class)
                    subclass(LoginScreenState.OauthAuthentication::class)
                    subclass(LoginScreenState.Accounts::class)
                    subclass(LoginScreenState.RecoverAccount::class)
                }
            }
    }

/**
 * Shows [destination], removing the loading screen and anything above it so back never returns to
 * it.
 */
internal fun MutableList<NavKey>.navigateTo(destination: LoginScreenState) {
    val loadingIndex = indexOf(LoginScreenState.Loading)
    add(destination)
    if (loadingIndex >= 0) {
        repeat(lastIndex - loadingIndex) { removeAt(loadingIndex) }
    }
}

/**
 * Removes the top screen.
 *
 * @return false when it is the only screen, so the caller can leave the login flow instead.
 */
internal fun MutableList<NavKey>.navigateUp(): Boolean =
    if (size > 1) {
        removeAt(lastIndex)
        true
    } else {
        false
    }
