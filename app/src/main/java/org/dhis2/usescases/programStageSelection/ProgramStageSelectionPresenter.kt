/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

    val compositeDisposable = CompositeDisposable()

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
                    view::setData,
                    Timber::e
                )
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun applyEffects(
        stageModels: List<ProgramStage>,
        calcResult: Result<RuleEffect>
    ): List<ProgramStage> = when {
        calcResult.error() != null -> stageModels
        else -> {
            val stageView = stageModels.associateBy({ it.uid() }, { it }).toMutableMap()
            ruleUtils.applyRuleEffects(stageView, calcResult)
            stageView.values.toList()
        }
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

    fun onBackClick() {
        view.back()
    }

    fun onDetach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }
}
