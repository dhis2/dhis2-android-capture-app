package org.dhis2.mobile.home.ui

import org.dhis2.mobile.home.resources.Res
import org.dhis2.mobile.home.resources.menu_manage_pin
import org.dhis2.mobile.home.resources.menu_set_pin
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeNavigationReelItemTest {
    @Test
    fun `list keeps the legacy menu order and hides developer options`() {
        assertEquals(
            listOf(
                HomeNavigationReelItem.Home,
                HomeNavigationReelItem.ImportSharedData,
                HomeNavigationReelItem.Settings,
                HomeNavigationReelItem.PinManagement(pinAlreadyCreated = false),
                HomeNavigationReelItem.Logout,
                HomeNavigationReelItem.Troubleshooting,
                HomeNavigationReelItem.About,
                HomeNavigationReelItem.RemoveAccount,
            ),
            HomeNavigationReelItem.list(displayDeveloperOptions = false, pinAlreadyCreated = false),
        )
    }

    @Test
    fun `list shows developer options before about when enabled`() {
        assertEquals(
            listOf(
                HomeNavigationReelItem.Home,
                HomeNavigationReelItem.ImportSharedData,
                HomeNavigationReelItem.Settings,
                HomeNavigationReelItem.PinManagement(pinAlreadyCreated = false),
                HomeNavigationReelItem.Logout,
                HomeNavigationReelItem.Troubleshooting,
                HomeNavigationReelItem.DeveloperOptions,
                HomeNavigationReelItem.About,
                HomeNavigationReelItem.RemoveAccount,
            ),
            HomeNavigationReelItem.list(displayDeveloperOptions = true, pinAlreadyCreated = false),
        )
    }

    @Test
    fun `pin management reads set pin without a pin and manage pin with one`() {
        assertEquals(Res.string.menu_set_pin, HomeNavigationReelItem.PinManagement(pinAlreadyCreated = false).label)
        assertEquals(Res.string.menu_manage_pin, HomeNavigationReelItem.PinManagement(pinAlreadyCreated = true).label)
    }

    @Test
    fun `developer options uses the design system doctor icon`() {
        assertEquals(
            DrawerIcon.DesignSystem("dhis2_doctor_positive"),
            HomeNavigationReelItem.DeveloperOptions.icon,
        )
    }
}
