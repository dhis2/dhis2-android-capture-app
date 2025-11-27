package org.dhis2.usescases.programStageSelection

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.form.model.EventMode
import org.dhis2.tracker.events.CreateEventUseCase
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class ProgramStageSelectionPresenter(
    private val view: ProgramStageSelectionView,
    private val programStageSelectionRepository: ProgramStageSelectionRepository,
    private val ruleUtils: RulesUtilsProvider,
    private val metadataIconProvider: MetadataIconProvider,
    private val schedulerProvider: SchedulerProvider,
    private val dispatcher: DispatcherProvider,
    private val createEventUseCase: CreateEventUseCase,
    private val d2ErrorUtils: D2ErrorUtils,
) {
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun onBackClick() {
        view.back()
    }

    fun programStages() {
        val stagesFlowable = programStageSelectionRepository.enrollmentProgramStages()
        val ruleEffectFlowable = programStageSelectionRepository.calculate()

        // Combining results of two repositories into a single stream.
        val stageModelsFlowable =
            Flowable.zip(
                stagesFlowable.subscribeOn(schedulerProvider.io()),
                ruleEffectFlowable.subscribeOn(schedulerProvider.io()),
            ) { stageModels, calcResult ->
                applyEffects(
                    stageModels,
                    calcResult,
                )
            }
        compositeDisposable.add(
            stageModelsFlowable
                .map { programStages ->
                    programStages.map { programStage ->
                        ProgramStageData(
                            programStage,
                            metadataIconProvider(programStage.style(), SurfaceColor.Primary),
                        )
                    }
                }.subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(this::handleProgramStages) { t: Throwable? ->
                    Timber.e(t)
                },
        )
    }

    private fun handleProgramStages(programStages: List<ProgramStageData>) {
        when (programStages.size) {
            1 ->
                view.setResult(
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
        calcResult: Result<List<RuleEffect>>,
    ): List<ProgramStage> {
        if (calcResult.isFailure) {
            Timber.e(calcResult.exceptionOrNull())
            return stageModels
        }
        val stageView = stageModels.associateBy { it.uid() }.toMutableMap()
        ruleUtils.applyRuleEffects(
            stageView,
            Result.success(calcResult.getOrDefault(emptyList())),
        )
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

    fun getStandardInterval(programStageUid: String): Int =
        programStageSelectionRepository.getStage(programStageUid)?.standardInterval() ?: 0

    fun onOrgUnitForNewEventSelected(
        programStageUid: String,
        programUid: String,
        orgUnitUid: String,
        enrollmentUid: String?,
    ) {
        CoroutineScope(dispatcher.io()).launch {
            createEventUseCase(
                programUid = programUid,
                orgUnitUid = orgUnitUid,
                programStageUid = programStageUid,
                enrollmentUid = enrollmentUid,
            ).fold(
                onSuccess = { eventUid ->
                    view.goToEventDetails(
                        eventUid = eventUid,
                        eventMode = EventMode.NEW,
                        programUid = programUid,
                    )
                },
                onFailure = {
                    view.displayMessage(d2ErrorUtils.getErrorMessage(it))
                },
            )
        }
    }
}
