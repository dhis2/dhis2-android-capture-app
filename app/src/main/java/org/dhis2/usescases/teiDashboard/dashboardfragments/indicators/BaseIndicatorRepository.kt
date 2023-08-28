package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import dhis2.org.analytics.charts.ui.IndicatorModel
import dhis2.org.analytics.charts.ui.LOCATION_FEEDBACK_WIDGET
import dhis2.org.analytics.charts.ui.LOCATION_INDICATOR_WIDGET
import dhis2.org.analytics.charts.ui.SectionTitle
import dhis2.org.analytics.charts.ui.SectionType
import io.reactivex.Flowable
import io.reactivex.Observable
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair
import org.hisp.dhis.rules.models.RuleActionDisplayText
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

abstract class BaseIndicatorRepository(
    open val d2: D2,
    open val ruleEngineRepository: RuleEngineRepository,
    open val programUid: String,
    open val resourceManager: ResourceManager,
) : IndicatorRepository {

    fun getIndicators(
        filter: Boolean = true,
        indicatorValueCalculator: (String) -> String,
    ): Flowable<List<AnalyticsModel>> {
        return d2.programModule().programIndicators()
            .byDisplayInForm().isTrue
            .byProgramUid().eq(programUid)
            .withLegendSets()
            .get()
            .toFlowable()
            .filter { filter }
            .map { indicators ->
                Observable.fromIterable(indicators)
                    .filter { it.displayInForm() != null && it.displayInForm()!! }
                    .map { indicator ->
                        val indicatorValue = try {
                            indicatorValueCalculator(indicator.uid())
                        } catch (e: Exception) {
                            Timber.e(e)
                            null
                        }
                        Pair.create(indicator, indicatorValue ?: "")
                    }.filter { it.val1().isNotEmpty() }
                    .flatMap {
                        getLegendColorForIndicator(it.val0(), it.val1())
                    }.map {
                        IndicatorModel(
                            it.val0(),
                            it.val1(),
                            it.val2(),
                            LOCATION_INDICATOR_WIDGET,
                            resourceManager.defaultIndicatorLabel(),
                        )
                    }
                    .toList()
            }.flatMap { it.toFlowable() }
    }

    fun getRulesIndicators(): Flowable<List<AnalyticsModel>> =
        d2.programModule().programRules().byProgramUid().eq(programUid).get()
            .map { UidsHelper.getUidsList(it) }
            .flatMap {
                d2.programModule().programRuleActions()
                    .byProgramRuleUid().`in`(it)
                    .byProgramRuleActionType().`in`(
                        ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                        ProgramRuleActionType.DISPLAYTEXT,
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
                            applyRuleEffectForIndicators(effects)
                        }
                }
            }

    private fun applyRuleEffectForIndicators(calcResult: Result<RuleEffect>): List<IndicatorModel> {
        val indicators = arrayListOf<IndicatorModel>()

        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            return arrayListOf()
        }

        for (ruleEffect in calcResult.items()) {
            val ruleAction = ruleEffect.ruleAction()
            if (ruleEffect.data()?.contains("#{") == false) {
                if (ruleAction is RuleActionDisplayKeyValuePair) {
                    val indicator = IndicatorModel(
                        ProgramIndicator.builder()
                            .uid((ruleAction).content())
                            .displayName((ruleAction).content())
                            .build(),
                        ruleEffect.data(),
                        "",
                        ruleAction.location(),
                        resourceManager.defaultIndicatorLabel(),
                    )

                    indicators.add(indicator)
                } else if (ruleAction is RuleActionDisplayText) {
                    val indicator = IndicatorModel(
                        ProgramIndicator.builder()
                            .uid(ruleAction.content())
                            .displayName(resourceManager.defaultIndicatorLabel())
                            .build(),
                        ruleAction.content() + ruleEffect.data(),
                        "",
                        ruleAction.location(),
                        resourceManager.defaultIndicatorLabel(),
                    )

                    indicators.add(indicator)
                }
            }
        }

        return indicators
    }

    private fun getLegendColorForIndicator(
        indicator: ProgramIndicator,
        value: String?,
    ): Observable<Trio<ProgramIndicator?, String?, String?>?>? {
        var color: String
        try {
            color = if (value?.toFloat()?.isNaN() == true) {
                ""
            } else {
                indicator.legendSets()?.let {
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
            }
        } catch (e: java.lang.Exception) {
            color = ""
        }

        return Observable.just(
            Trio.create<ProgramIndicator, String, String>(
                indicator,
                value,
                color,
            ),
        )
    }

    fun arrangeSections(
        indicators: List<AnalyticsModel>,
        ruleIndicators: List<AnalyticsModel>,
        charts: List<AnalyticsModel> = emptyList(),
    ): List<AnalyticsModel> {
        return mutableListOf<AnalyticsModel>().apply {
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
                    },
                )
            }.sortedBy { (it as IndicatorModel).programIndicator?.displayName() }
            if (indicatorList.isNotEmpty() && charts.isNotEmpty()) {
                add(SectionTitle(resourceManager.sectionChartsAndIndicators()))
                add(SectionTitle(resourceManager.sectionIndicators(), SectionType.SUBSECTION))
                addAll(indicatorList)
                addAll(charts)
            } else if (indicatorList.isNotEmpty() && charts.isEmpty()) {
                add(SectionTitle(resourceManager.sectionIndicators()))
                addAll(indicatorList)
            } else if (indicatorList.isEmpty() && charts.isNotEmpty()) {
                add(SectionTitle(resourceManager.sectionCharts()))
                addAll(charts)
            }
        }
    }
}
