package org.dhis2.usescases.programStageSelection

import androidx.annotation.VisibleForTesting
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
        enrollmentUid: String
    ) {
        compositeDisposable.add(
            Flowable.zip<List<ProgramStage>, Result<RuleEffect>, List<ProgramStage>>(
                programStageSelectionRepository.enrollmentProgramStages(
                    programId,
                    enrollmentUid
                ),
                programStageSelectionRepository.calculate(),
                BiFunction { stageModels, calcResult ->
                    applyEffects(stageModels, calcResult)
                }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setData(it) },
                    { Timber.e(it) }
                )
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun applyEffects(
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

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    fun onProgramStageClick(programStage: ProgramStage) {
        if (programStage.access().data().write()) {
            view.setResult(
                programStage.uid(),
                programStage.repeatable(),
                programStage.periodType()
            )
        } else {
            displayMessage(null)
        }
    }

    fun getStandardInterval(programStageUid: String): Int {
        return programStageSelectionRepository.getStage(programStageUid).standardInterval() ?: 0
    }
}
