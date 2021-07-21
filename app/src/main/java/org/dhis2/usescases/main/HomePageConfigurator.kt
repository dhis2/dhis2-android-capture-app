package org.dhis2.usescases.main

import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class HomePageConfigurator(
    private val homeRepository: HomeRepository,
    private val featureConfigRepository: FeatureConfigRepository
) : NavigationPageConfigurator {
    override fun displayTasks(): Boolean {
        return super.displayTasks()
    }

    override fun displayPrograms(): Boolean {
        return true
    }

    override fun displayAnalytics(): Boolean {
        return homeRepository.hasHomeAnalytics() &&
            featureConfigRepository.isFeatureEnable(Feature.ANDROAPP_2557_VG)
    }
}
