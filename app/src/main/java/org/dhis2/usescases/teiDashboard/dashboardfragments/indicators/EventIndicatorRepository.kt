package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.D2

class EventIndicatorRepository(
    d2: D2,
    ruleEngineHelper: RuleEngineHelper?,
    programUid: String,
    val eventUid: String,
    resourceManager: ResourceManager,
) : BaseIndicatorRepository(d2, ruleEngineHelper, programUid, resourceManager) {
    override suspend fun fetchData(): List<AnalyticsModel> {
        val indicators = getIndicators { indicatorUid ->
            d2
                .programModule()
                .programIndicatorEngine()
                .getEventProgramIndicatorValue(
                    eventUid,
                    indicatorUid,
                ) ?: ""
        }.blockingFirst(emptyList())

        val ruleIndicators = getRulesIndicators().blockingFirst(emptyList())

        return arrangeSections(indicators, ruleIndicators)
    }
}
