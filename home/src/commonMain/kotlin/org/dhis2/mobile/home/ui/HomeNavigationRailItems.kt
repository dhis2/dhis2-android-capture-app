package org.dhis2.mobile.home.ui

import org.dhis2.mobile.home.resources.Res
import org.dhis2.mobile.home.resources.ic_delete
import org.dhis2.mobile.home.resources.ic_doctor_positive
import org.dhis2.mobile.home.resources.ic_menu_home
import org.dhis2.mobile.home.resources.ic_menu_info
import org.dhis2.mobile.home.resources.ic_menu_lock
import org.dhis2.mobile.home.resources.ic_menu_logout
import org.dhis2.mobile.home.resources.ic_menu_settings
import org.dhis2.mobile.home.resources.ic_qr
import org.dhis2.mobile.home.resources.ic_troubleshooting
import org.dhis2.mobile.home.resources.menu_about
import org.dhis2.mobile.home.resources.menu_dev_options
import org.dhis2.mobile.home.resources.menu_home
import org.dhis2.mobile.home.resources.menu_import_shared_data
import org.dhis2.mobile.home.resources.menu_log_out
import org.dhis2.mobile.home.resources.menu_remove_account
import org.dhis2.mobile.home.resources.menu_set_pin
import org.dhis2.mobile.home.resources.menu_settings
import org.dhis2.mobile.home.resources.menu_troubleshooting
import org.jetbrains.compose.resources.DrawableResource
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
    val icon: DrawableResource,
    val label: StringResource,
    val group: MainDrawerNavigationGroup,
) {
    data object Home : HomeNavigationReelItem(
        Res.drawable.ic_menu_home,
        Res.string.menu_home,
        MainDrawerNavigationGroup.MAIN,
    )

    data object ImportSharedData : HomeNavigationReelItem(
        Res.drawable.ic_qr,
        Res.string.menu_import_shared_data,
        MainDrawerNavigationGroup.ACTIONS,
    )

    data object Settings : HomeNavigationReelItem(
        Res.drawable.ic_menu_settings,
        Res.string.menu_settings,
        MainDrawerNavigationGroup.ACTIONS,
    )

    data object PinManagement : HomeNavigationReelItem(
        Res.drawable.ic_menu_lock,
        Res.string.menu_set_pin,
        MainDrawerNavigationGroup.ACTIONS,
    )

    data object Logout : HomeNavigationReelItem(
        Res.drawable.ic_menu_logout,
        Res.string.menu_log_out,
        MainDrawerNavigationGroup.ACTIONS,
    )

    data object Troubleshooting : HomeNavigationReelItem(
        Res.drawable.ic_troubleshooting,
        Res.string.menu_troubleshooting,
        MainDrawerNavigationGroup.EXTRA,
    )

    data object DeveloperOptions : HomeNavigationReelItem(
        Res.drawable.ic_doctor_positive,
        Res.string.menu_dev_options,
        MainDrawerNavigationGroup.EXTRA,
    )

    data object About : HomeNavigationReelItem(
        Res.drawable.ic_menu_info,
        Res.string.menu_about,
        MainDrawerNavigationGroup.EXTRA,
    )

    data object RemoveAccount : HomeNavigationReelItem(
        Res.drawable.ic_delete,
        Res.string.menu_remove_account,
        MainDrawerNavigationGroup.ACCOUNT,
    )

    companion object {
        /**
         * Items of the drawer, in display order.
         *
         * @param displayDeveloperOptions whether to show [DeveloperOptions]; the legacy menu only
         *   shows it in debug builds and in the training flavour.
         */
        fun list(displayDeveloperOptions: Boolean): List<HomeNavigationReelItem> =
            buildList {
                add(Home)
                add(ImportSharedData)
                add(Settings)
                add(PinManagement)
                add(Logout)
                add(Troubleshooting)
                if (displayDeveloperOptions) add(DeveloperOptions)
                add(About)
                add(RemoveAccount)
            }
    }
}
