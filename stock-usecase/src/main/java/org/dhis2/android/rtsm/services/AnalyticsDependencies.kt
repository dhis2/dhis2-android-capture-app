package org.dhis2.android.rtsm.services

import android.content.Context
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2

class AnalyticsDependencies(
    private val appContext: Context,
    private val d2: D2,
    private val featureConfigRepository: FeatureConfigRepository,
    private val colorUtils: ColorUtils,
    private val dispatcherProvider: DispatcherProvider,
) : Charts.Dependencies {
    override fun getContext(): Context = appContext

    override fun getD2(): D2 = d2

    override fun getFeatureConfigRepository(): FeatureConfigRepository = featureConfigRepository

    override fun getColorUtils(): ColorUtils = colorUtils

    override fun getChartDispatcher(): DispatcherProvider = dispatcherProvider
}
