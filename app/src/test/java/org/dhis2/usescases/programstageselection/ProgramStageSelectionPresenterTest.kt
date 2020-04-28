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

package org.dhis2.usescases.programstageselection

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import java.lang.Exception
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionContract
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionPresenter
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionRepository
import org.dhis2.utils.Result
import org.dhis2.utils.RulesUtilsProvider
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleActionHideProgramStage
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ProgramStageSelectionPresenterTest {

    private lateinit var presenter: ProgramStageSelectionPresenter

    private val view: ProgramStageSelectionContract.View = mock()
    private val repository: ProgramStageSelectionRepository = mock()
    private val rulesUtils: RulesUtilsProvider = mock()
    private val scheduler = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = ProgramStageSelectionPresenter(view, repository, rulesUtils, scheduler)
    }

    @Test
    fun `Should set programStages`() {
        val programId = "programId"
        val enrollmentUid = "programUid"
        val programStages = listOf(ProgramStage.builder().uid("programStage").build())
        val calcResult = Result.success(
            listOf(
                RuleEffect.create(
                    RuleActionHideProgramStage.create("programStage")
                )
            )
        )

        whenever(
            repository.enrollmentProgramStages()
        ) doReturn Flowable.just(programStages)
        whenever(repository.calculate()) doReturn Flowable.just(calcResult)
        whenever(
            rulesUtils.applyRuleEffects(
                programStages.associateBy({ it.uid() }, { it }).toMutableMap(),
                calcResult
            )
        ) doAnswer { null }

        presenter.getProgramStages(programId, enrollmentUid)

        verify(view).setData(programStages)
    }

    @Test
    fun `Should hide programStage when app`() {
        val programStages: MutableList<ProgramStage> =
            mutableListOf(ProgramStage.builder().uid("programStage").build())
        val calcResult = Result.success(
            listOf(
                RuleEffect.create(
                    RuleActionHideProgramStage.create("programStage")
                )
            )
        )

        whenever(
            rulesUtils.applyRuleEffects(
                programStages.associateBy({ it.uid() }, { it }).toMutableMap(),
                calcResult
            )
        ) doAnswer {
            it.getArgument<MutableMap<String, ProgramStage>>(0).remove("programStage")
            null
        }

        Assert.assertEquals(presenter.applyEffects(programStages, calcResult).size, 0)
    }

    @Test
    fun `Should do nothing when rule effect has error`() {
        val programStages: MutableList<ProgramStage> =
            mutableListOf(ProgramStage.builder().uid("programStage").build())
        val calcResult: Result<RuleEffect> = Result.failure(
            Exception("error")
        ) as Result<RuleEffect>

        Assert.assertEquals(
            presenter.applyEffects(programStages, calcResult),
            programStages
        )
    }

    @Test
    fun `Should set program stage when it is clicked`() {
        val programStage = ProgramStage.builder()
            .uid("programStage")
            .repeatable(false)
            .periodType(PeriodType.Monthly)
            .access(Access.create(true, true, DataAccess.create(true, true)))
            .build()

        presenter.onProgramStageClick(programStage)

        verify(view).setResult(
            programStage.uid(),
            programStage.repeatable()!!,
            programStage.periodType()
        )
    }

    @Test
    fun `Should go back when back button is clicked`() {
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should dispose of all disposables`() {
        presenter.onDettach()

        val disposableSize = presenter.compositeDisposable.size()

        Assert.assertTrue(disposableSize == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }
}
