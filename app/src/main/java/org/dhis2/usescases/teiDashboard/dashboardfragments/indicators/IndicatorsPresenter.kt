package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.Charts
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.data.analytics.ChartModel
import org.dhis2.data.analytics.IndicatorModel
import org.dhis2.data.analytics.LOCATION_FEEDBACK_WIDGET
import org.dhis2.data.analytics.LOCATION_INDICATOR_WIDGET
import org.dhis2.data.analytics.SectionTitle
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.utils.DhisTextUtils
import org.dhis2.utils.Result
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair
import org.hisp.dhis.rules.models.RuleActionDisplayText
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class IndicatorsPresenter(
    val d2: D2,
    val programUid: String,
    val teiUid: String,
    val dashboardRepository: DashboardRepository,
    private val ruleEngineRepository: RuleEngineRepository,
    val schedulerProvider: SchedulerProvider,
    val view: IndicatorsView,
    val charts: Charts?,
    val resources: ResourceManager
) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var enrollmentUid: String

    init {
        var enrollmentRepository = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
        if (!DhisTextUtils.isEmpty(programUid)) {
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid)
        }

        enrollmentUid = if (enrollmentRepository.one().blockingGet() == null) {
            ""
        } else {
            enrollmentRepository.one().blockingGet().uid()
        }
    }

    fun init() {
        compositeDisposable.add(
            Flowable.zip<List<AnalyticsModel>?,
                List<AnalyticsModel>?,
                List<AnalyticsModel>,
                List<AnalyticsModel>>(
                getIndicators(),
                getRulesIndicators(),
                Flowable.just(
                    charts?.getCharts(enrollmentUid)?.map { ChartModel(it) }
                ),
                Function3 { indicators, ruleIndicators, charts ->
                    mutableListOf<AnalyticsModel>().apply {
                        val feedbackList = ruleIndicators.filter {
                            it is IndicatorModel && it.location == LOCATION_FEEDBACK_WIDGET
                        }.sortedBy { (it as IndicatorModel).programIndicator?.displayName() }
                        if (feedbackList.isNotEmpty()) {
                            add(SectionTitle("Feedback"))
                            addAll(feedbackList)
                        }
                        val indicatorList = indicators.toMutableList().apply {
                            addAll(
                                ruleIndicators.filter {
                                    it is IndicatorModel &&
                                        it.location == LOCATION_INDICATOR_WIDGET
                                }
                            )
                        }.sortedBy { (it as IndicatorModel).programIndicator?.displayName() }
                        if (indicatorList.isNotEmpty()) {
                            add(SectionTitle("Indicators"))
                            addAll(indicatorList)
                        }
                        if (charts.isNotEmpty()) {
                            add(SectionTitle("Charts"))
                            addAll(charts)
                        }
                    }
                }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.swapAnalytics(it) },
                    { Timber.d(it) }
                )
        )
    }

    private fun getIndicators(): Flowable<List<AnalyticsModel>> {
        return dashboardRepository.getIndicators(programUid)
            .filter { !DhisTextUtils.isEmpty(enrollmentUid) }
            .map { indicators ->
                Observable.fromIterable(indicators)
                    .filter { it.displayInForm() != null && it.displayInForm()!! }
                    .map { indicator ->
                        val indicatorValue = try {
                            d2.programModule()
                                .programIndicatorEngine().getProgramIndicatorValue(
                                    enrollmentUid,
                                    null,
                                    indicator.uid()
                                )
                        } catch (e: Exception) {
                            Timber.e(e)
                            null
                        }
                        return@map Pair.create(indicator, indicatorValue ?: "")
                    }.filter { it.val1().isNotEmpty() }
                    .flatMap {
                        dashboardRepository
                            .getLegendColorForIndicator(it.val0(), it.val1())
                    }.map {
                        IndicatorModel(
                            it.val0(),
                            it.val1(),
                            it.val2(),
                            LOCATION_INDICATOR_WIDGET
                        )
                    }
                    .toList()
            }.flatMap { it.toFlowable() }
    }

    private fun getRulesIndicators(): Flowable<List<AnalyticsModel>> =
        d2.programModule().programRules().byProgramUid().eq(programUid).get()
            .map { UidsHelper.getUidsList(it) }
            .flatMap {
                d2.programModule().programRuleActions()
                    .byProgramRuleUid().`in`(it)
                    .byProgramRuleActionType().`in`(
                        ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                        ProgramRuleActionType.DISPLAYTEXT
                    )
                    .get()
            }
            .flatMapPublisher { ruleAction ->
                if (ruleAction.isEmpty()) {
                    return@flatMapPublisher Flowable.just<List<AnalyticsModel>>(listOf())
                } else {
                    return@flatMapPublisher ruleEngineRepository.updateRuleEngine()
                        .flatMap { ruleEngineRepository.reCalculate() }
                        .map { effects ->
                            // Restart rule engine to take into account value changes
                            applyRuleEffects(effects)
                        }
                }
            }

    private fun applyRuleEffects(calcResult: Result<RuleEffect>): List<IndicatorModel> {
        val indicators = arrayListOf<IndicatorModel>()

        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            return arrayListOf()
        }

        for (ruleEffect in calcResult.items()) {
            val ruleAction = ruleEffect.ruleAction()
            if (!ruleEffect.data().contains("#{")) {
                if (ruleAction is RuleActionDisplayKeyValuePair) {
                    val indicator = IndicatorModel(
                        ProgramIndicator.builder()
                            .uid((ruleAction).content())
                            .displayName((ruleAction).content())
                            .build(),
                        ruleEffect.data(),
                        "",
                        ruleAction.location()
                    )

                    indicators.add(indicator)
                } else if (ruleAction is RuleActionDisplayText) {
                    val indicator = IndicatorModel(
                        null,
                        ruleAction.content() + ruleEffect.data(),
                        "",
                        ruleAction.location()
                    )
                    indicators.add(indicator)
                }
            }
        }

        return indicators
    }

    fun onDettach() = compositeDisposable.clear()

    fun displayMessage(message: String) = view.displayMessage(message)
}
