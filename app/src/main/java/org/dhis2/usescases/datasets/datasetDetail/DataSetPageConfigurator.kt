package org.dhis2.usescases.datasets.datasetDetail

import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class DataSetPageConfigurator(
    private val dataSetDetailRepository: DataSetDetailRepository,
    private val featureConfigRepository: FeatureConfigRepository
) : NavigationPageConfigurator {

    override fun displayListView(): Boolean {
        return true
    }

    override fun displayAnalytics(): Boolean {
        return dataSetDetailRepository.dataSetHasAnalytics() &&
            featureConfigRepository.isFeatureEnable(Feature.ANDROAPP_2557) ||
            featureConfigRepository.isFeatureEnable(Feature.ANDROAPP_2557_VG)
    }
}
