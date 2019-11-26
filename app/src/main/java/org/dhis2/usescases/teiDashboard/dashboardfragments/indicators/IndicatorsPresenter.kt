package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.text.TextUtils.isEmpty
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair
import org.hisp.dhis.rules.models.RuleActionDisplayText
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class IndicatorsPresenter(val d2: D2,
                          val schedulerProvider: SchedulerProvider,
                          val programUid: String,
                          val teiUid: String,
                          val dashboardRepository: DashboardRepository,
                          val ruleEngineRepository: RuleEngineRepository,
                          val view: IndicatorsView) {

    private val compositeDisposable = CompositeDisposable()
    private var enrollmentUid : String

    init {
        var enrollmentRepository = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)

        if (!isEmpty(programUid))
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid)

        enrollmentUid = if(enrollmentRepository.one().blockingGet() == null)
                            ""
                        else
                            enrollmentRepository.one().blockingGet().uid()

    }

    fun init() {
        compositeDisposable.add(dashboardRepository.getIndicators(programUid)
            .filter{
                !isEmpty(enrollmentUid)
            }.map {
                Observable.fromIterable(it)
                    .filter{ indicator ->
                        (indicator.displayInForm() != null && indicator.displayInForm()!!)
                    }.map { indicator ->
                        val indicatorValue =
                            d2.programModule().programIndicatorEngine().getProgramIndicatorValue(
                                enrollmentUid,
                                null,
                                indicator.uid())
                        return@map Pair.create(indicator, indicatorValue?: "")
                    }.filter { pair -> !pair.val1().isNullOrEmpty() }
                    .flatMap { pair -> dashboardRepository
                                    .getLegendColorForIndicator(pair.val0(), pair.val1()) }
                    .toList()
            }.flatMap { it.toFlowable() }
            .flatMap {
                ruleEngineRepository.updateRuleEngine()
                .flatMap { ruleEngineRepository.reCalculate() }
                .map { this::applyRuleEffects }
                .map {

                }}
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {view::swapIndicators},
                Timber::e
        ))
    }

    private fun applyRuleEffects(calcResult: Result<RuleEffect>):
            List<Trio<ProgramIndicator, String, String>>{

        var indicators: MutableList<Trio<ProgramIndicator, String, String>> = mutableListOf()

        if(calcResult.error() != null){
            Timber.e(calcResult.error())
            return listOf()
        }

        calcResult.items().forEach { ruleEffect ->
            val ruleAction = ruleEffect.ruleAction()
            if(!ruleEffect.data().contains("#{")) { //Avoid display unavailable variables
                if(ruleAction is RuleActionDisplayKeyValuePair){
                    val indicator = Trio.create(
                        ProgramIndicator.builder()
                            .uid(ruleAction.content())
                            .displayName(ruleAction.content())
                            .build(),
                        ruleEffect.data(), "")

                    indicators.add(indicator)
                }else if(ruleAction is RuleActionDisplayText){
                    val indicator: Trio<ProgramIndicator, String, String> = Trio.create(null,
                        ruleAction.content() + ruleEffect.data(), "")
                    indicators.add(indicator)
                }
            }
        }

        return indicators
    }
}