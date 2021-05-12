package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.FlowableProcessor
import junit.framework.Assert.assertTrue
import org.dhis2.data.forms.FormSectionViewModel
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
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
    private val preferences: PreferenceProvider = mock()
    private val getNextVisibleSection: GetNextVisibleSection = GetNextVisibleSection()
    private val eventFieldMapper: EventFieldMapper = mock()
    private val onRowActionProcessor: FlowableProcessor<RowAction> = mock()
    private val fieldFactory: FieldViewModelFactory = mock()

    @Before
    fun setUp() {
        presenter = EventCapturePresenterImpl(
            view,
            eventUid,
            eventRepository,
            rulesUtilProvider,
            valueStore,
            schedulers,
            preferences,
            getNextVisibleSection,
            eventFieldMapper,
            onRowActionProcessor,
            fieldFactory.sectionProcessor()
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
        ) doReturn StoreResult("fieldUid", ValueStoreResult.VALUE_CHANGED)
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
        ) doReturn StoreResult("fieldUid", ValueStoreResult.VALUE_CHANGED)
        presenter.setOptionGroupToHide("optionGroupToHide", false, "field")
        verify(valueStore).deleteOptionValueIfSelectedInGroup("field", "optionGroupToHide", false)
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
                ObjectStyle.builder().build(),
                false,
                "any",
                null
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
                ObjectStyle.builder().build(),
                false,
                "any",
                null
            )
        )

        assertTrue(section.isEmpty())
    }

    @Test
    fun `Should return current section if sectionsToHide is empty`() {
        val activeSection = getNextVisibleSection.get("activeSection", sections())
        assertTrue(activeSection == "activeSection")
    }

    @Test
    fun `Should return current when section is last one and hide section is not empty`() {
        val activeSection = getNextVisibleSection.get("sectionUid_3", sections())
        assertTrue(activeSection == "sectionUid_3")
    }

    private fun sections(): MutableList<FormSectionViewModel> {
        return arrayListOf(
            FormSectionViewModel.createForSection(
                "eventUid",
                "sectionUid_1",
                "sectionName_1",
                null
            ),
            FormSectionViewModel.createForSection(
                "eventUid",
                "sectionUid_2",
                "sectionName_2",
                null
            ),
            FormSectionViewModel.createForSection(
                "eventUid",
                "sectionUid_3",
                "sectionName_3",
                null
            )
        )
    }
}
