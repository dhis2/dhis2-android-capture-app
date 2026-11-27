package org.dhis2.mobile.home.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import org.dhis2.mobile.home.resources.Res
import org.dhis2.mobile.home.resources.menu_about
import org.dhis2.mobile.home.resources.menu_dev_options
import org.dhis2.mobile.home.resources.menu_home
import org.dhis2.mobile.home.resources.menu_import_shared_data
import org.dhis2.mobile.home.resources.menu_log_out
import org.dhis2.mobile.home.resources.menu_manage_pin
import org.dhis2.mobile.home.resources.menu_remove_account
import org.dhis2.mobile.home.resources.menu_set_pin
import org.dhis2.mobile.home.resources.menu_settings
import org.dhis2.mobile.home.resources.menu_troubleshooting
import org.jetbrains.compose.resources.StringResource

/**
 * Sections of the home navigation drawer, matching the groups of the legacy `main_menu.xml`.
 * The drawer shows a divider between items of different groups.
 */
enum class MainDrawerNavigationGroup {
    MAIN,
    ACTIONS,
    EXTRA,
    ACCOUNT,
}

sealed class HomeNavigationReelItem(
    val icon: DrawerIcon,
    val label: StringResource,
    val group: MainDrawerNavigationGroup,
) {
    data object Home : HomeNavigationReelItem(
        DrawerIcon.Vector(Icons.Filled.Home),
        Res.string.menu_home,
        MainDrawerNavigationGroup.MAIN,
    )

    data object ImportSharedData : HomeNavigationReelItem(
        DrawerIcon.Vector(Icons.Filled.QrCodeScanner),
        Res.string.menu_import_shared_data,
        MainDrawerNavigationGroup.ACTIONS,
    )

    data object Settings : HomeNavigationReelItem(
        DrawerIcon.Vector(Icons.Filled.Settings),
        Res.string.menu_settings,
        MainDrawerNavigationGroup.ACTIONS,
    )

    /**
     * Locks the session, creating a PIN first if there is none.
     *
     * @param pinAlreadyCreated whether a PIN exists; the item then reads "Manage PIN".
     */
    data class PinManagement(
        val pinAlreadyCreated: Boolean,
    ) : HomeNavigationReelItem(
            DrawerIcon.Vector(Icons.Filled.Lock),
            if (pinAlreadyCreated) Res.string.menu_manage_pin else Res.string.menu_set_pin,
            MainDrawerNavigationGroup.ACTIONS,
        )

    data object Logout : HomeNavigationReelItem(
        DrawerIcon.Vector(Icons.AutoMirrored.Filled.ExitToApp),
        Res.string.menu_log_out,
        MainDrawerNavigationGroup.ACTIONS,
    )

    data object Troubleshooting : HomeNavigationReelItem(
        DrawerIcon.Vector(Icons.Filled.Handyman),
        Res.string.menu_troubleshooting,
        MainDrawerNavigationGroup.EXTRA,
    )

    data object DeveloperOptions : HomeNavigationReelItem(
        DrawerIcon.DesignSystem("dhis2_doctor_positive"),
        Res.string.menu_dev_options,
        MainDrawerNavigationGroup.EXTRA,
    )

    data object About : HomeNavigationReelItem(
        DrawerIcon.Vector(Icons.Filled.Info),
        Res.string.menu_about,
        MainDrawerNavigationGroup.EXTRA,
    )

    data object RemoveAccount : HomeNavigationReelItem(
        DrawerIcon.Vector(Icons.Filled.Delete),
        Res.string.menu_remove_account,
        MainDrawerNavigationGroup.ACCOUNT,
    )

    companion object {
        /**
         * Items of the drawer, in display order.
         *
         * @param displayDeveloperOptions whether to show [DeveloperOptions]; the legacy menu only
         *   shows it in debug builds and in the training flavour.
         * @param pinAlreadyCreated whether the user has a PIN, which changes the
         *   [PinManagement] label.
         */
        fun list(
            displayDeveloperOptions: Boolean,
            pinAlreadyCreated: Boolean,
        ): List<HomeNavigationReelItem> =
            buildList {
                add(Home)
                add(ImportSharedData)
                add(Settings)
                add(PinManagement(pinAlreadyCreated))
                add(Logout)
                add(Troubleshooting)
                if (displayDeveloperOptions) add(DeveloperOptions)
                add(About)
                add(RemoveAccount)
            }
    }
}
