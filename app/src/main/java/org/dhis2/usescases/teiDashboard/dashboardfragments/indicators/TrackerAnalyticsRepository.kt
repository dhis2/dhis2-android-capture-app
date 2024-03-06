package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.ui.AnalyticsModel
import dhis2.org.analytics.charts.ui.ChartModel
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class TrackerAnalyticsRepository(
    d2: D2,
    ruleEngineHelper: RuleEngineHelper?,
    val charts: Charts?,
    programUid: String,
    val teiUid: String,
    resourceManager: ResourceManager,
) : BaseIndicatorRepository(d2, ruleEngineHelper, programUid, resourceManager) {

    val enrollmentUid: String

    init {
        var enrollmentRepository = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
        if (!DhisTextUtils.isEmpty(programUid)) {
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid)
        }

        enrollmentUid = enrollmentRepository.one().blockingGet()?.uid() ?: ""
    }

    override fun fetchData(): Flowable<List<AnalyticsModel>> {
        return Flowable.zip<
            List<AnalyticsModel>?,
            List<AnalyticsModel>?,
            List<AnalyticsModel>,
            List<AnalyticsModel>,
            >(
            getIndicators(
                !DhisTextUtils.isEmpty(enrollmentUid),
            ) { indicatorUid ->
                d2.programModule()
                    .programIndicatorEngine().getEnrollmentProgramIndicatorValue(
                        enrollmentUid,
                        indicatorUid,
                    )
            },
            getRulesIndicators(),
            Flowable.just(
                charts?.geEnrollmentCharts(enrollmentUid)?.map { ChartModel(it) },
            ),
            Function3 { indicators, ruleIndicators, charts ->
                arrangeSections(indicators, ruleIndicators, charts)
            },
        )
    }

    override fun filterByPeriod(
        chartModel: ChartModel,
        selectedPeriods: List<RelativePeriod>,
        lineListingColumnId: Int?,
    ) {
        chartModel.graph.visualizationUid?.let { visualizationUid ->
            charts?.setVisualizationPeriods(visualizationUid, lineListingColumnId, selectedPeriods)
        }
    }

    override fun filterByOrgUnit(
        chartModel: ChartModel,
        selectedOrgUnits: List<OrganisationUnit>,
        filterType: OrgUnitFilterType,
        lineListingColumnId: Int?,
    ) {
        chartModel.graph.visualizationUid?.let { visualizationUid ->
            charts?.setVisualizationOrgUnits(
                visualizationUid,
                lineListingColumnId,
                selectedOrgUnits,
                filterType,
            )
        }
    }

    override fun filterLineListing(chartModel: ChartModel, value: String?) {
        charts?.setLineListingFilter(chartModel.uid, -1, value)
    }
}
