package org.dhis2.mobile.home.ui

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
                HomeNavigationReelItem.PinManagement,
                HomeNavigationReelItem.Logout,
                HomeNavigationReelItem.Troubleshooting,
                HomeNavigationReelItem.About,
                HomeNavigationReelItem.RemoveAccount,
            ),
            HomeNavigationReelItem.list(displayDeveloperOptions = false),
        )
    }

    @Test
    fun `list shows developer options before about when enabled`() {
        assertEquals(
            listOf(
                HomeNavigationReelItem.Home,
                HomeNavigationReelItem.ImportSharedData,
                HomeNavigationReelItem.Settings,
                HomeNavigationReelItem.PinManagement,
                HomeNavigationReelItem.Logout,
                HomeNavigationReelItem.Troubleshooting,
                HomeNavigationReelItem.DeveloperOptions,
                HomeNavigationReelItem.About,
                HomeNavigationReelItem.RemoveAccount,
            ),
            HomeNavigationReelItem.list(displayDeveloperOptions = true),
        )
    }

    @Test
    fun `developer options uses the design system doctor icon`() {
        assertEquals(
            DrawerIcon.DesignSystem("dhis2_doctor_positive"),
            HomeNavigationReelItem.DeveloperOptions.icon,
        )
    }
}
