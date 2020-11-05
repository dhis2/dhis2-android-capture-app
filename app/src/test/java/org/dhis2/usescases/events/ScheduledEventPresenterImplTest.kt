package org.dhis2.usescases.events

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ScheduledEventPresenterImplTest {

    lateinit var presenter: ScheduledEventPresenterImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val view: ScheduledEventContract.View = mock()

    @Before
    fun setUp() {
        presenter = ScheduledEventPresenterImpl(view, d2, "eventUid")
    }

    @Test
    fun `Should open event initial screen if the event needs coordinates`() {
        whenever(
            d2.eventModule().events().uid(any())
        ) doReturn mock()
        whenever(
            d2.eventModule().events().uid(any()).blockingGet()
        ) doReturn Event.builder()
            .uid("eventUid")
            .programStage("stageUid").build()
        whenever(
            d2.programModule().programStages().uid(any())
                .blockingGet()
        ) doReturn ProgramStage.builder()
            .uid("stageUid")
            .program(ObjectWithUid.create("programUid"))
            .featureType(FeatureType.POINT)
            .build()
        whenever(
            d2.programModule().programs().uid(any()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .categoryCombo(ObjectWithUid.create("catComboUid"))
            .build()
        whenever(
            d2.categoryModule().categoryCombos().uid(any()).blockingGet()
        ) doReturn CategoryCombo.builder()
            .uid("catComboUid")
            .isDefault(true)
            .build()

        presenter.setEventDate(Date())

        verify(view).openInitialActivity()
    }

    @Test
    fun `Should open event initial screen if the event needs cat combo`() {
        whenever(
            d2.eventModule().events().uid(any())
        ) doReturn mock()
        whenever(
            d2.eventModule().events().uid(any()).blockingGet()
        ) doReturn Event.builder()
            .uid("eventUid")
            .programStage("stageUid").build()
        whenever(
            d2.programModule().programStages().uid(any())
                .blockingGet()
        ) doReturn ProgramStage.builder()
            .uid("stageUid")
            .program(ObjectWithUid.create("programUid"))
            .featureType(FeatureType.NONE)
            .build()
        whenever(
            d2.programModule().programs().uid(any()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .categoryCombo(ObjectWithUid.create("catComboUid"))
            .build()
        whenever(
            d2.categoryModule().categoryCombos().uid(any()).blockingGet()
        ) doReturn CategoryCombo.builder()
            .uid("catComboUid")
            .isDefault(false)
            .build()

        presenter.setEventDate(Date())

        verify(view).openInitialActivity()
    }

    @Test
    fun `Should open event initial screen if the event needs cat combo and coordinates`() {
        whenever(
            d2.eventModule().events().uid(any())
        ) doReturn mock()
        whenever(
            d2.eventModule().events().uid(any()).blockingGet()
        ) doReturn Event.builder()
            .uid("eventUid")
            .programStage("stageUid").build()
        whenever(
            d2.programModule().programStages().uid(any())
                .blockingGet()
        ) doReturn ProgramStage.builder()
            .uid("stageUid")
            .program(ObjectWithUid.create("programUid"))
            .featureType(FeatureType.POINT)
            .build()
        whenever(
            d2.programModule().programs().uid(any()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .categoryCombo(ObjectWithUid.create("catComboUid"))
            .build()
        whenever(
            d2.categoryModule().categoryCombos().uid(any()).blockingGet()
        ) doReturn CategoryCombo.builder()
            .uid("catComboUid")
            .isDefault(false)
            .build()

        presenter.setEventDate(Date())

        verify(view).openInitialActivity()
    }

    @Test
    fun `Should open event form screen if the event does not need coordinates or catCombo`() {
        whenever(
            d2.eventModule().events().uid(any())
        ) doReturn mock()
        whenever(
            d2.eventModule().events().uid(any()).blockingGet()
        ) doReturn Event.builder()
            .uid("eventUid")
            .programStage("stageUid").build()
        whenever(
            d2.programModule().programStages().uid(any())
                .blockingGet()
        ) doReturn ProgramStage.builder()
            .uid("stageUid")
            .program(ObjectWithUid.create("programUid"))
            .featureType(FeatureType.NONE)
            .build()
        whenever(
            d2.programModule().programs().uid(any()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .categoryCombo(ObjectWithUid.create("catComboUid"))
            .build()
        whenever(
            d2.categoryModule().categoryCombos().uid(any()).blockingGet()
        ) doReturn CategoryCombo.builder()
            .uid("catComboUid")
            .isDefault(true)
            .build()

        presenter.setEventDate(Date())

        verify(view).openFormActivity()
    }
}
