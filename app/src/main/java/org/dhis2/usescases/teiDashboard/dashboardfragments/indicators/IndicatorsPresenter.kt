package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
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
    val view: IndicatorsView
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
            Flowable.zip<List<Trio<ProgramIndicator, String, String>>?,
                List<Trio<ProgramIndicator, String, String>>?,
                List<Trio<ProgramIndicator, String, String>>>(
                getIndicators(),
                getRulesIndicators(),
                BiFunction { indicators, ruleIndicators ->
                    val indicatorsMutable = indicators.toMutableList()
                    for (indicator in ruleIndicators) {
                        if (!indicators.contains(indicator)) {
                            indicatorsMutable.add(indicator)
                        }
                    }
                    return@BiFunction indicatorsMutable.toList()
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

    private fun getIndicators(): Flowable<List<Trio<ProgramIndicator, String, String>>> {
        return dashboardRepository.getIndicators(programUid)
            .filter { !DhisTextUtils.isEmpty(enrollmentUid) }
            .map {
                indicators ->
                Observable.fromIterable(indicators)
                    .filter { it.displayInForm() != null && it.displayInForm()!! }
                    .map { indicator ->
                        val indicatorValue = d2.programModule()
                            .programIndicatorEngine().getProgramIndicatorValue(
                            enrollmentUid,
                            null,
                            indicator.uid()
                        )
                        return@map Pair.create(indicator, indicatorValue ?: "")
                    }.filter { !it.val1().isEmpty() }
                    .flatMap {
                        dashboardRepository
                            .getLegendColorForIndicator(it.val0(), it.val1())
                    }
                    .toList()
            }.flatMap { it.toFlowable() }
    }

    private fun getRulesIndicators(): Flowable<List<Trio<ProgramIndicator, String, String>>> =
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
                    return@flatMapPublisher Flowable.just<List<Trio<ProgramIndicator,
                                String, String>>>(listOf())
                } else {
                    return@flatMapPublisher ruleEngineRepository.updateRuleEngine()
                        .flatMap { ruleEngineRepository.reCalculate() }
                        .map {
                            // Restart rule engine to take into account value changes
                            applyRuleEffects(it)
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
