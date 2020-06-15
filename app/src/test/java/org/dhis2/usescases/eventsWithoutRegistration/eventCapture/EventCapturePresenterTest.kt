package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.dhis2.data.forms.dataentry.StoreResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.RulesUtilsProvider
import org.hisp.dhis.android.core.common.ObjectStyle
import org.junit.Before
import org.junit.Test

class EventCapturePresenterTest {
    private lateinit var presenter: EventCapturePresenterImpl
    private val view: EventCaptureContract.View = mock()
    private val eventUid = "eventUid"
    private val eventRepository: EventCaptureContract.EventCaptureRepository = mock()
    private val rulesUtilProvider: RulesUtilsProvider = mock()
    private val valueStore: ValueStore = mock()
    private val schedulers = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = EventCapturePresenterImpl(
            view,
            eventUid,
            eventRepository,
            rulesUtilProvider,
            valueStore,
            schedulers
        )
    }

    @Test
    fun `Should delete option value if selected in group to hide`() {
        whenever(
            eventRepository.getOptionsFromGroups(arrayListOf("optionGroupToHide"))
        ) doReturn arrayListOf(
            "option1",
            "option2"
        )
        whenever(
            valueStore.deleteOptionValueIfSelectedInGroup(
                "field",
                "optionGroupToHide",
                true
            )
        ) doReturn StoreResult("fieldUid", ValueStoreImpl.ValueStoreResult.VALUE_CHANGED)
        presenter.setOptionGroupToHide("optionGroupToHide", true, "field")
        verify(valueStore).deleteOptionValueIfSelectedInGroup("field", "optionGroupToHide", true)
    }

    @Test
    fun `Should delete option value if selected not in group to hide`() {
        whenever(
            valueStore.deleteOptionValueIfSelectedInGroup(
                "field",
                "optionGroupToHide",
                false
            )
        ) doReturn StoreResult("fieldUid", ValueStoreImpl.ValueStoreResult.VALUE_CHANGED)
        presenter.setOptionGroupToHide("optionGroupToHide", false, "field")
        verify(valueStore).deleteOptionValueIfSelectedInGroup("field", "optionGroupToHide", false)
    }

    @Test
    fun `Display field should return display section`() {
        val section = presenter.getFieldSection(
            DisplayViewModel.create("", "", "", "")
        )

        assertTrue(section == "display")
    }

    @Test
    fun `Field with section should return its section`() {
        val section = presenter.getFieldSection(
            SpinnerViewModel.create(
                "ID",
                "Label",
                "options",
                false,
                "optionSet",
                null,
                "testSection",
                false,
                null,
                1,
                ObjectStyle.builder().build()
            )
        )

        assertTrue(section == "testSection")
    }

    @Test
    fun `Field with no section should return empty section`() {
        val section = presenter.getFieldSection(
            SpinnerViewModel.create(
                "ID",
                "Label",
                "options",
                false,
                "optionSet",
                null,
                null,
                false,
                null,
                1,
                ObjectStyle.builder().build()
            )
        )

        assertTrue(section.isEmpty())
    }
}
