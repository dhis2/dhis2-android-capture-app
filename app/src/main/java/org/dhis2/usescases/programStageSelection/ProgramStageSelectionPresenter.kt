package org.dhis2.usescases.programStageSelection

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.Result
import org.dhis2.utils.RulesUtilsProvider
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class ProgramStageSelectionPresenter(
    private var view: ProgramStageSelectionView,
    private val programStageSelectionRepository: ProgramStageSelectionRepository,
    private val ruleUtils: RulesUtilsProvider,
    private val schedulerProvider: SchedulerProvider
) {

    private val compositeDisposable = CompositeDisposable()

    fun getProgramStages(
        programId: String,
        uid: String
    ) {
        val stagesFlowable = programStageSelectionRepository.enrollmentProgramStages(programId, uid)
        val ruleEffectFlowable = programStageSelectionRepository.calculate()

        val stageModelsFlowable =
            Flowable.zip<List<ProgramStage>, Result<RuleEffect>, List<ProgramStage>>(
                stagesFlowable.subscribeOn(schedulerProvider.io()),
                ruleEffectFlowable.subscribeOn(schedulerProvider.io()),
                BiFunction { stageModels, calcResult ->
                    this.applyEffects(
                        stageModels,
                        calcResult
                    )
                }
            )

        compositeDisposable.add(
            stageModelsFlowable
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setData(it) },
                    { Timber.e(it) }
                )
        )
    }

    private fun applyEffects(
        stageModels: List<ProgramStage>,
        calcResult: Result<RuleEffect>
    ): List<ProgramStage> =
        when {
            calcResult.error() != null -> stageModels
            else -> {
                val stageView = stageModels.associateBy({ it.uid() }, { it }).toMutableMap()
                ruleUtils.applyRuleEffects(stageView, calcResult)
                stageView.values.toList()
            }
        }

    fun onBackClick() {
        view.back()
    }

    fun onDetach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun onProgramStageClick(programStage: ProgramStage) {
        view.setResult(programStage.uid(), programStage.repeatable(), programStage.periodType())
    }

    fun getStandardInterval(programStageUid: String): Int {
        return programStageSelectionRepository.getStage(programStageUid).standardInterval() ?: 0
    }
}
