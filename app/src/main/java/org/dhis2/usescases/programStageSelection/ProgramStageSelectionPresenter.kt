package org.dhis2.usescases.programStageSelection

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class ProgramStageSelectionPresenter(
    private val view: ProgramStageSelectionView,
    private val programStageSelectionRepository: ProgramStageSelectionRepository,
    private val ruleUtils: RulesUtilsProvider,
    private val metadataIconProvider: MetadataIconProvider,
    private val schedulerProvider: SchedulerProvider,
) {
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun onBackClick() {
        view.back()
    }

    fun programStages() {
        val stagesFlowable = programStageSelectionRepository.enrollmentProgramStages()
        val ruleEffectFlowable = programStageSelectionRepository.calculate()

        // Combining results of two repositories into a single stream.
        val stageModelsFlowable = Flowable.zip(
            stagesFlowable.subscribeOn(schedulerProvider.io()),
            ruleEffectFlowable.subscribeOn(schedulerProvider.io()),
        ) { stageModels: List<ProgramStage>, calcResult: Result<RuleEffect> ->
            applyEffects(
                stageModels,
                calcResult,
            )
        }
        compositeDisposable.add(
            stageModelsFlowable.map { programStages ->
                programStages.map { programStage ->
                    ProgramStageData(
                        programStage,
                        metadataIconProvider(programStage.style()),
                    )
                }
            }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(this::handleProgramStages) { t: Throwable? ->
                    Timber.e(t)
                },
        )
    }

    private fun handleProgramStages(programStages: List<ProgramStageData>) {
        when (programStages.size) {
            1 -> view.setResult(
                programStageUid = programStages.first().programStage.uid(),
                repeatable = programStages.first().programStage.repeatable() == true,
                periodType = programStages.first().programStage.periodType(),
            )

            else -> view.setData(programStages)
        }
    }

    @VisibleForTesting
    fun applyEffects(
        stageModels: List<ProgramStage>,
        calcResult: Result<RuleEffect>,
    ): List<ProgramStage> {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            return stageModels
        }
        val stageView = stageModels.map { it.uid() to it }.toMap().toMutableMap()
        ruleUtils.applyRuleEffects(stageView, kotlin.Result.success(calcResult.items()))
        return ArrayList(stageView.values)
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    fun onProgramStageClick(programStage: ProgramStage) {
        if (programStage.access().data().write()) {
            view.setResult(
                programStage.uid(),
                programStage.repeatable() == true,
                programStage.periodType(),
            )
        } else {
            view.displayMessage(null)
        }
    }

    fun getStandardInterval(programStageUid: String): Int {
        return programStageSelectionRepository.getStage(programStageUid)?.standardInterval() ?: 0
    }
}
