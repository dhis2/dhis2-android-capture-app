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

import io.reactivex.Flowable
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.programStageSelection.ProgramStageData
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionPresenter
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionRepository
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionView
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProgramStageSelectionPresenterTest {

    private lateinit var presenter: ProgramStageSelectionPresenter

    private val view: ProgramStageSelectionView = mock()
    private val repository: ProgramStageSelectionRepository = mock()
    private val rulesUtils: RulesUtilsProvider = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private val metadataIconProvider: MetadataIconProvider = mock {
        on { invoke(any(), any<String>(), any()) } doReturn MetadataIconData.defaultIcon()
    }

    @Before
    fun setUp() {
        presenter = ProgramStageSelectionPresenter(
            view,
            repository,
            rulesUtils,
            metadataIconProvider,
            scheduler,
        )
    }

    @Test
    fun `Should set programStages`() {
        val programStages = listOf(
            ProgramStage.builder().uid("programStage1").build(),
            ProgramStage.builder().uid("programStage2").build(),
        )
        val programStageData = listOf(
            ProgramStageData(
                ProgramStage.builder().uid("programStage1").build(),
                MetadataIconData.defaultIcon(),
            ),
            ProgramStageData(
                ProgramStage.builder().uid("programStage2").build(),
                MetadataIconData.defaultIcon(),
            ),
        )
        val calcResult = Result.success(
            listOf(
                RuleEffect(
                    "ruleUid",
                    RuleAction(
                        data = null,
                        type = ProgramRuleActionType.HIDEPROGRAMSTAGE.name,
                        values = mutableMapOf(
                            Pair("programStage", "programStage"),
                        ),
                    ),
                ),
            ),
        )

        whenever(
            repository.enrollmentProgramStages(),
        ) doReturn Flowable.just(programStages)
        whenever(repository.calculate()) doReturn Flowable.just(calcResult)
        whenever(
            rulesUtils.applyRuleEffects(
                programStages.associateBy({ it.uid() }, { it }).toMutableMap(),
                kotlin.Result.success(calcResult.items()),
            ),
        ) doAnswer { null }

        presenter.programStages()

        verify(view).setData(programStageData)
    }

    @Test
    fun `Should go to programStage when there is only one`() {
        val programStage = ProgramStage.builder()
            .uid("programStage")
            .repeatable(true)
            .periodType(PeriodType.Daily)
            .build()
        val programStages = listOf(programStage)
        val calcResult = Result.success(
            listOf(
                RuleEffect(
                    ruleId = "ruleUid",
                    ruleAction = RuleAction(
                        data = null,
                        type = ProgramRuleActionType.HIDEPROGRAMSTAGE.name,
                        values = mutableMapOf(
                            Pair("programStage", "programStage"),
                        ),
                    ),
                ),
            ),
        )

        whenever(
            repository.enrollmentProgramStages(),
        ) doReturn Flowable.just(programStages)
        whenever(repository.calculate()) doReturn Flowable.just(calcResult)
        whenever(
            rulesUtils.applyRuleEffects(
                programStages.associateBy({ it.uid() }, { it }).toMutableMap(),
                kotlin.Result.success(calcResult.items()),
            ),
        ) doAnswer { null }

        presenter.programStages()

        verify(view).setResult(
            programStage.uid(),
            programStage.repeatable() == true,
            programStage.periodType(),
        )
    }

    @Test
    fun `Should hide programStage when app`() {
        val programStages: MutableList<ProgramStage> =
            mutableListOf(ProgramStage.builder().uid("programStage").build())
        val calcResult = Result.success(
            listOf(
                RuleEffect(
                    "ruleUid",
                    RuleAction(
                        data = null,
                        type = ProgramRuleActionType.HIDEPROGRAMSTAGE.name,
                        values = mutableMapOf(
                            Pair("programStage", "programStage"),
                        ),
                    ),
                ),
            ),
        )

        whenever(
            rulesUtils.applyRuleEffects(
                programStages.associateBy({ it.uid() }, { it }).toMutableMap(),
                kotlin.Result.success(calcResult.items()),
            ),
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
            Exception("error"),
        ) as Result<RuleEffect>

        Assert.assertEquals(
            presenter.applyEffects(programStages, calcResult),
            programStages,
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

    @Test
    fun `Should set result when clicking on a ProgramStage`() {
        val programStage = ProgramStage.builder().uid("programStage").access(
            Access.builder().data(DataAccess.builder().write(true).build()).build(),
        ).build()

        presenter.onProgramStageClick(programStage)

        verify(view).setResult("programStage", false, null)
    }

    @Test
    fun `Should display permission message when clicking on a ProgramStage without access`() {
        val programStage = ProgramStage.builder().uid("programStage").access(
            Access.builder().data(DataAccess.builder().write(false).build()).build(),
        ).build()

        presenter.onProgramStageClick(programStage)

        verify(view).displayMessage(null)
    }

    @Test
    fun `Should return the standard interval`() {
        val interval = 3
        whenever(repository.getStage("programUid")) doReturn mock()
        whenever(repository.getStage("programUid")?.standardInterval()) doReturn interval

        val result = presenter.getStandardInterval("programUid")

        assert(interval == result)
    }

    @Test
    fun `Should return 0 if the standard interval is not configured`() {
        val interval = null
        whenever(repository.getStage("programUid")) doReturn mock()
        whenever(repository.getStage("programUid")?.standardInterval()) doReturn interval

        val result = presenter.getStandardInterval("programUid")

        assert(result == 0)
    }
}
