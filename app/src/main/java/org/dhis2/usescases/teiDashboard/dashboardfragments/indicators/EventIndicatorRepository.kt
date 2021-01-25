package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.data.analytics.IndicatorModel
import org.dhis2.data.analytics.LOCATION_FEEDBACK_WIDGET
import org.dhis2.data.analytics.LOCATION_INDICATOR_WIDGET
import org.dhis2.data.analytics.SectionTitle
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Trio
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

class EventIndicatorRepository(
    val d2: D2,
    val ruleEngineRepository: RuleEngineRepository,
    val programUid: String,
    val eventUid: String,
    val resourceManager: ResourceManager
) : IndicatorRepository {

    override fun fetchData(): Flowable<List<AnalyticsModel>> {
        return Flowable.zip<List<AnalyticsModel>?,
            List<AnalyticsModel>?,
            List<AnalyticsModel>>(
            getIndicators(),
            getRulesIndicators(),
            BiFunction { indicators, ruleIndicators ->
                mutableListOf<AnalyticsModel>().apply {
                    val feedbackList = ruleIndicators.filter {
                        it is IndicatorModel && it.location == LOCATION_FEEDBACK_WIDGET
                    }.sortedBy { (it as IndicatorModel).programIndicator?.displayName() }
                    if (feedbackList.isNotEmpty()) {
                        add(SectionTitle(resourceManager.sectionFeedback()))
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
                        add(SectionTitle(resourceManager.sectionIndicators()))
                        addAll(indicatorList)
                    }
                }
            }
        )
    }

    private fun getIndicators(): Flowable<List<AnalyticsModel>> {
        return d2.programModule().programIndicators()
            .byDisplayInForm().isTrue
            .byProgramUid().eq(programUid)
            .withLegendSets()
            .get().toFlowable()
            .map { indicators ->
                Observable.fromIterable(indicators)
                    .filter { it.displayInForm() != null && it.displayInForm()!! }
                    .map { indicator ->
                        val indicatorValue = try {
                            d2.programModule()
                                .programIndicatorEngine().getEventProgramIndicatorValue(
                                    eventUid,
                                    indicator.uid()
                                )
                        } catch (e: Exception) {
                            Timber.e(e)
                            null
                        }
                        Pair.create(indicator, indicatorValue ?: "")
                    }.filter { it.val1().isNotEmpty() }
                    .flatMap {
                        getLegendColorForIndicator(it.val0(), it.val1())
                    }.map {
                        IndicatorModel(it.val0(), it.val1(), it.val2(), LOCATION_INDICATOR_WIDGET)
                    }
                    .toList()
            }.flatMap { it.toFlowable() }
    }

    private fun getLegendColorForIndicator(
        indicator: ProgramIndicator,
        value: String?
    ): Observable<Trio<ProgramIndicator?, String?, String?>?>? {
        val color = indicator.legendSets()?.let {
            if (it.isNotEmpty()) {
                val legends = d2.legendSetModule().legends().byStartValue()
                    .smallerThan(value?.toDouble() ?: 0.0).byEndValue()
                    .biggerThan(value?.toDouble() ?: 0.0)
                    .byLegendSet().eq(it.first().uid()).blockingGet()
                if (legends.isNotEmpty()) {
                    legends.first().color() ?: ""
                } else {
                    ""
                }
            } else {
                ""
            }
        } ?: ""

        return Observable.just(
            Trio.create<ProgramIndicator, String, String>(
                indicator,
                value,
                color
            )
        )
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

    private fun applyRuleEffects(
        calcResult: Result<RuleEffect>
    ): List<AnalyticsModel> {
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
}
