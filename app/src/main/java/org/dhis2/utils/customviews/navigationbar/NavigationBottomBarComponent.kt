package org.dhis2.utils.customviews.navigationbar

import dagger.Subcomponent

@Subcomponent(modules = [NavigationBottomBarModule::class])
interface NavigationBottomBarComponent {
    fun inject(navigationBottomBar: NavigationBottomBar)
}
