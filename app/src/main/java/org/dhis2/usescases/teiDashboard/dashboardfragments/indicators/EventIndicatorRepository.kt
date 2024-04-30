package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.hisp.dhis.android.core.D2

class EventIndicatorRepository(
    d2: D2,
    ruleEngineRepository: RuleEngineRepository,
    programUid: String,
    val eventUid: String,
    resourceManager: ResourceManager,
) : BaseIndicatorRepository(d2, ruleEngineRepository, programUid, resourceManager) {

    override fun fetchData(): Flowable<List<AnalyticsModel>> {
        return Flowable.zip<
            List<AnalyticsModel>?,
            List<AnalyticsModel>?,
            List<AnalyticsModel>,
            >(
            getIndicators { indicatorUid ->
                d2.programModule()
                    .programIndicatorEngine().getEventProgramIndicatorValue(
                        eventUid,
                        indicatorUid,
                    )
            },
            getRulesIndicators(),
            BiFunction { indicators, ruleIndicators ->
                arrangeSections(indicators, ruleIndicators)
            },
        )
    }
}
