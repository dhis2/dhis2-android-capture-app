package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.Charts
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.data.analytics.IndicatorModel
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.utils.DhisTextUtils
import org.dhis2.utils.Result
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
    val charts: Charts?
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
        // TODO: THIS MUST BE REMOVED FOR RELEASE
        view.displayMessage("Analytic module is ready: ${charts?.hasCharts() ?: false} ")

        compositeDisposable.add(
            Flowable.zip<List<AnalyticsModel>?,
                    List<AnalyticsModel>?,
                    List<AnalyticsModel>>(
                getIndicators(),
                getRulesIndicators(),
                BiFunction { indicators, ruleIndicators ->
                    val indicatorsMutable = indicators.toMutableList()
                    for (indicator in ruleIndicators) {
                        if (!indicators.contains(indicator)) {
                            indicatorsMutable.add(indicator)
                        }
                    }
                    return@BiFunction indicatorsMutable.sortedBy { (it as IndicatorModel).programIndicator?.displayName() }
                }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.swapIndicators(it) },
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
                        IndicatorModel(it.val0(), it.val1(), it.val2())
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
                            applyRuleEffects(effects).map {
                                IndicatorModel(it.val0(), it.val1(), it.val2())
                            }
                        }
                }
            }

    private fun applyRuleEffects(calcResult: Result<RuleEffect>):
            List<Trio<ProgramIndicator, String, String>> {
        val indicators = arrayListOf<Trio<ProgramIndicator, String, String>>()

        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            return arrayListOf()
        }

        for (ruleEffect in calcResult.items()) {
            val ruleAction = ruleEffect.ruleAction()
            if (!ruleEffect.data().contains("#{")) {
                if (ruleAction is RuleActionDisplayKeyValuePair) {
                    val indicator = Trio.create(
                        ProgramIndicator.builder()
                            .uid((ruleAction).content())
                            .displayName((ruleAction).content())
                            .build(),
                        ruleEffect.data(), ""
                    )

                    indicators.add(indicator)
                } else if (ruleAction is RuleActionDisplayText) {
                    val indicator: Trio<ProgramIndicator, String, String> = Trio.create(
                        null,
                        ruleAction.content() + ruleEffect.data(), ""
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
