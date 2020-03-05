package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.indicators

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract.EventCaptureRepository
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair
import org.hisp.dhis.rules.models.RuleActionDisplayText
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.util.ArrayList

class EventIndicatorsPresenter internal constructor(
    private val d2: D2,
    private val programUid: String,
    private val eventUid: String,
    private val eventCaptureRepository: EventCaptureRepository,
    private val ruleEngineRepository: RuleEngineRepository,
    private val schedulerProvider: SchedulerProvider
) : EventIndicatorsContracts.Presenter {

    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var view: EventIndicatorsContracts.View

    override fun init(view: EventIndicatorsContracts.View) {
        this.view = view
        compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            eventCaptureRepository.getIndicators(programUid)
                .map { indicators ->
                    Observable.fromIterable(indicators)
                        .filter { indicator ->
                            indicator.displayInForm() != null && indicator.displayInForm()!!
                        }
                        .map { indicator ->

                            val indicatorValue = d2.programModule().programIndicatorEngine()
                                .getProgramIndicatorValue(
                                    null, eventUid,
                                    indicator.uid()
                                )

                            Pair.create(indicator, indicatorValue ?: "")
                        }
                        .filter { pair -> pair.val1().isNotEmpty() }
                        .flatMap { pair ->
                            eventCaptureRepository.getLegendColorForIndicator(
                                pair.val0(),
                                pair.val1()
                            )
                        }
                        .toList()
                }
                .flatMap { obj -> obj.toFlowable() }
                .flatMap { indicators ->
                    ruleEngineRepository.updateRuleEngine()
                        .flatMap { ruleEngineRepository.reCalculate() }
                        .map { calcResult -> applyRuleEffects(calcResult) } //Restart rule engine to take into account value changes
                        .map { ruleIndicators ->
                            for (indicator in ruleIndicators) {
                                if (!indicators.contains(indicator)) {
                                    indicators.add(indicator)
                                }
                            }
                            indicators
                        }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view.swapIndicators(),
                    Consumer { t -> Timber.d(t) }
                )
        )
    }

    private fun applyRuleEffects(calcResult: Result<RuleEffect>): List<Trio<ProgramIndicator, String, String>> {
        val indicators: MutableList<Trio<ProgramIndicator, String, String>> =
            ArrayList()
        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            return ArrayList()
        }
        for (ruleEffect in calcResult.items()) {
            val ruleAction = ruleEffect.ruleAction()
            if (!ruleEffect.data().contains("#{")) //Avoid display unavailable variables
                if (ruleAction is RuleActionDisplayKeyValuePair) {
                    val indicator =
                        Trio.create(
                            ProgramIndicator.builder()
                                .uid(ruleAction.content())
                                .displayName(ruleAction.content())
                                .build(),
                            ruleEffect.data(), ""
                        )
                    indicators.add(indicator)
                } else if (ruleAction is RuleActionDisplayText) {
                    val indicator =
                        Trio.create<ProgramIndicator, String, String>(
                            null,
                            ruleAction.content() + ruleEffect.data(), ""
                        )
                    indicators.add(indicator)
                }
        }
        return indicators
    }

    override fun onDettach() {
        compositeDisposable.clear()
    }

    override fun displayMessage(message: String) {
        view.displayMessage(message)
    }
}