package org.dhis2.usescases.programstageselection

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionPresenter
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionRepository
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionView
import org.dhis2.utils.Result
import org.dhis2.utils.RulesUtilsProvider
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleActionHideFieldTests
import org.hisp.dhis.rules.models.RuleActionHideProgramStage
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ProgramStageSelectionPresenterTest {

    private lateinit var presenter: ProgramStageSelectionPresenter

    private val view: ProgramStageSelectionView = mock()
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

        whenever(repository.enrollmentProgramStages(
            programId,
            enrollmentUid
        )) doReturn Flowable.just(programStages)
        whenever(repository.calculate()) doReturn Flowable.just(calcResult)
        whenever(presenter.applyEffects(programStages, calcResult)) doReturn programStages

        presenter.getProgramStages(programId, enrollmentUid)

        verify(view).setData(programStages)
    }

    @Test
    fun `Should hide programStage when app`(){

        val programStages = listOf(ProgramStage.builder().uid("programStage").build())
        val calcResult = Result.success(
            listOf(
                RuleEffect.create(
                    RuleActionHideProgramStage.create("programStage")
                )
            )
        )
        Assert.assertEquals(
            presenter.applyEffects(programStages, calcResult),
            emptyList<ProgramStage>()
        )
    }



}