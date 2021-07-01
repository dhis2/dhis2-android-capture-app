package org.dhis2.utils.customviews.navigationbar

import dagger.Module
import dagger.Provides
import org.hisp.dhis.android.core.D2

@Module
class NavigationBottomBarModule {

    @Provides
    fun provideNavigationRepository(d2: D2): NavigationBottomBarRepository {
        return NavigationBottomBarRepositoryImpl(d2)
    }
}
