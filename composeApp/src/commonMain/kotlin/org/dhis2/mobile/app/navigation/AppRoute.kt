package org.dhis2.mobile.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Splash : AppRoute

    @Serializable
    data object Login : AppRoute

    @Serializable
    data object Home : AppRoute
}

/**
 * Lets the back stack be saved on targets without reflection (iOS), where every [NavKey] must be
 * registered for polymorphic serialization.
 */
internal val appRouteSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.Splash::class)
                    subclass(AppRoute.Login::class)
                    subclass(AppRoute.Home::class)
                }
            }
    }
