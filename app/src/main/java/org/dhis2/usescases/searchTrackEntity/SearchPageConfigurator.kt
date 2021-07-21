package org.dhis2.usescases.searchTrackEntity

import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class SearchPageConfigurator(
    val searchRepository: SearchRepository,
    val featureConfigRepository: FeatureConfigRepository
) : NavigationPageConfigurator {

    override fun displayListView(): Boolean {
        return true
    }

    override fun displayTableView(): Boolean {
        return false
    }

    override fun displayMapView(): Boolean {
        return searchRepository.programHasCoordinates()
    }

    override fun displayAnalytics(): Boolean {
        return searchRepository.programHasAnalytics() && featureConfigRepository.isFeatureEnable(
            Feature.ANDROAPP_2557
        ) || featureConfigRepository.isFeatureEnable(Feature.ANDROAPP_2557_VG)
    }
}
