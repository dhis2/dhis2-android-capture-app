package org.dhis2.usescases.datasets.datasetDetail

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class DataSetPageConfigurator(
    private val dataSetDetailRepository: DataSetDetailRepository,
) : NavigationPageConfigurator {

    private var canDisplayAnalytics: Boolean = false

    fun initVariables(): DataSetPageConfigurator {
        canDisplayAnalytics = dataSetDetailRepository.dataSetHasAnalytics()
        return this
    }

    override fun displayListView(): Boolean {
        return true
    }

    override fun displayAnalytics(): Boolean {
        return canDisplayAnalytics
    }
}
