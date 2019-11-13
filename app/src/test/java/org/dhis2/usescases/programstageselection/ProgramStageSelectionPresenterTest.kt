package org.dhis2.usescases.programstageselection

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import java.lang.Exception
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionPresenter
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionRepository
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionView
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

        whenever(
            repository.enrollmentProgramStages(
                programId,
                enrollmentUid
            )
        ) doReturn Flowable.just(programStages)
        whenever(repository.calculate()) doReturn Flowable.just(calcResult)
        whenever(presenter.applyEffects(programStages, calcResult)) doReturn programStages

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
            rulesUtils.applyProgramStageRuleEffects(
                programStages,
                calcResult
            )
        ) doReturn emptyList()

        Assert.assertEquals(
            presenter.applyEffects(programStages, calcResult).size,
            0
        )
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
            programStage.repeatable(),
            programStage.periodType()
        )
    }

    @Test
    fun `Should display message when it is clicked with no access`() {
        val programStage = ProgramStage.builder()
            .uid("programStage")
            .repeatable(false)
            .periodType(PeriodType.Monthly)
            .access(Access.create(true, true, DataAccess.create(true, false)))
            .build()

        presenter.onProgramStageClick(programStage)

        verify(view).displayMessage(null)
    }

    @Test
    fun `Should go back when back button is clicked`() {
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should dispose of all disposables`() {
        presenter.onDetach()

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
