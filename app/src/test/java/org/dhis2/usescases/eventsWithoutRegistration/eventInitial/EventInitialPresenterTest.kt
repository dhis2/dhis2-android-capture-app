package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import android.app.DatePickerDialog
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventFieldMapper
import org.dhis2.utils.DateUtils
import org.dhis2.utils.EventCreationType
import org.dhis2.utils.Result
import org.dhis2.utils.RulesUtilsProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventInitialPresenterTest {
    lateinit var presenter: EventInitialPresenter
    private val view: EventInitialContract.View = mock()
    private val rulesUtilsProvider: RulesUtilsProvider = mock()
    private val eventInitialRepository: EventInitialRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val preferences: PreferenceProvider = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val eventFieldMapper: EventFieldMapper = mock()

    @Before
    fun setUp() {
        presenter = EventInitialPresenter(
            view,
            rulesUtilsProvider,
            eventInitialRepository,
            schedulers,
            preferences,
            analyticsHelper,
            matomoAnalyticsController,
            eventFieldMapper
        )
    }

    @Test
    fun `Should initialize when creating a new event`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        initMocks("uid", null, "stageUid", catCombo)

        presenter.init("uid", null, "orgUnit", "stageUid")
        verify(view).setAccessDataWrite(true)
        verify(view).setProgram(any())
        verify(view).setCatComboOptions(catCombo, listOf(), null)
        verify(view).setProgramStage(any())
        verify(view).setOrgUnit(any(), any())
    }

    @Test
    fun `Should initialize when opening an event`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        initMocks("uid", "eventId", "stageUid", catCombo)

        presenter.init("uid", "eventId", "orgUnit", "stageUid")
        verify(view).setAccessDataWrite(true)
        verify(view).setProgram(any())
        verify(view).setProgramStage(any())
        verify(view).setEvent(any())
        verify(view).setCatComboOptions(catCombo, listOf(), null)
        verify(view).updatePercentage(any())
        verify(view).setOrgUnit(any(), any())
    }

    @Test
    fun `Should init previous org unit if exist and no one is selected`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn true
        whenever(preferences.getString(Preference.CURRENT_ORG_UNIT, null)) doReturn "prevOrgUnit"
        assertTrue(presenter.getCurrentOrgUnit(null) == "prevOrgUnit")
    }

    @Test
    fun `Should init previous org unit if exist and one is selected`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn true
        whenever(preferences.getString(Preference.CURRENT_ORG_UNIT, null)) doReturn "prevOrgUnit"
        assertTrue(presenter.getCurrentOrgUnit("orgUnit") == "prevOrgUnit")
    }

    @Test
    fun `Should init given org unit if previous does not exist`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn false
        assertTrue(presenter.getCurrentOrgUnit("orgUnit") == "orgUnit")
    }

    @Test
    fun `Should init null org unit if previous does not exist`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn false
        assertTrue(presenter.getCurrentOrgUnit(null).isNullOrEmpty())
    }

    @Test
    fun `Should show QR code when clicking on share button`() {
        presenter.onShareClick()
        verify(view).showQR()
    }

    @Test
    fun `Should delete event when the delete option is clicked`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        initMocks("uid", "event", "stageUid", catCombo)

        presenter.init("uid", "event", "orgUnit", "stage")
        presenter.deleteEvent("tei_uid")
        verify(eventInitialRepository).deleteEvent(any(), any())
        verify(view).showEventWasDeleted()
    }

    @Test
    fun `Should show message when event is not yet created and the delete option is clicked`() {
        whenever(view.context) doReturn mock()
        whenever(view.context.getString(any())) doReturn "display message"

        presenter.deleteEvent("tei_uid")
        verify(view).displayMessage(any())
    }

    @Test
    fun `Should return true if enrollment is open`() {
        whenever(eventInitialRepository.isEnrollmentOpen) doReturn true
        assertTrue(presenter.isEnrollmentOpen)
    }

    @Test
    fun `Should return false if enrollment is not open`() {
        whenever(eventInitialRepository.isEnrollmentOpen) doReturn false
        assertFalse(presenter.isEnrollmentOpen)
    }

    @Test
    fun `Should get the programStage ObjectStyle`() {
        val style = ObjectStyle.builder().color("color").icon("icon").build()
        whenever(eventInitialRepository.getObjectStyle("stage")) doReturn Observable.just(style)

        presenter.getStageObjectStyle("stage")
        verify(view).renderObjectStyle(style)
    }

    @Test
    fun `Should get programStage`() {
        val programStage = ProgramStage.builder().uid("stage").build()
        whenever(
            eventInitialRepository.programStageWithId("stage")
        ) doReturn Observable.just(programStage)

        presenter.getProgramStage("stage")
        verify(view).setProgramStage(programStage)
    }

    @Test
    fun `Should go back on closing a previously saved event`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        initMocks("uid", "event", "stageUid", catCombo)

        presenter.init("uid", "event", "orgUnit", "stage")
        presenter.onBackClick()
        verify(preferences).removeValue(Preference.EVENT_COORDINATE_CHANGED)
        verify(analyticsHelper).setEvent(any(), any(), any())
        verify(view).back()
    }

    @Test
    fun `Should go back without analytics when new event`() {
        presenter.onBackClick()
        verify(preferences).removeValue(Preference.EVENT_COORDINATE_CHANGED)
        verify(view).back()
    }

    @Test
    fun `Should create an event`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        val date = Date()
        initMocks("uid", null, "stage", catCombo)
        whenever(
            eventInitialRepository.createEvent(
                "enrollment",
                "tei",
                "uid",
                "stage",
                date,
                "orgUnit",
                "catCombo",
                "catOption",
                geometry
            )
        ) doReturn Observable.just("event")

        presenter.init("uid", null, "orgUnit", "stage")
        presenter.createEvent(
            "enrollment", "stage", date, "orgUnit", "catCombo", "catOption", geometry, "tei"
        )

        verify(view).onEventCreated("event")
    }

    @Test
    fun `Should show error when there is an error creating an event`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        val date = Date()
        initMocks("uid", null, "stage", catCombo)
        whenever(
            eventInitialRepository.createEvent(
                "enrollment",
                "tei",
                "uid",
                "stage",
                date,
                "orgUnit",
                "catCombo",
                "catOption",
                geometry
            )
        ) doReturn Observable.error(Throwable("Error"))

        presenter.init("uid", null, "orgUnit", "stage")
        presenter.createEvent(
            "enrollment", "stage", date, "orgUnit", "catCombo", "catOption", geometry, "tei"
        )

        verify(view).renderError("Error")
    }

    @Test
    fun `Should create an schedule event permanent`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        val date = Date()
        initMocks("uid", null, "stage", catCombo)
        whenever(
            eventInitialRepository.scheduleEvent(
                "enrollment",
                null,
                "uid",
                "stage",
                date,
                "orgUnit",
                "catCombo",
                "catOption",
                geometry
            )
        ) doReturn Observable.just("event")

        presenter.init("uid", null, "orgUnit", "stage")
        presenter.scheduleEventPermanent(
            "enrollment", null, "stage", date, "orgUnit", "catCombo", "catOption", geometry
        )

        verify(view).onEventCreated("event")
    }

    @Test
    fun `Should how error when there is an problem creating a scheduled event permanent`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        val date = Date()
        initMocks("uid", null, "stage", catCombo)
        whenever(
            eventInitialRepository.scheduleEvent(
                "enrollment",
                null,
                "uid",
                "stage",
                date,
                "orgUnit",
                "catCombo",
                "catOption",
                geometry
            )
        ) doReturn Observable.error(Throwable("Error"))

        presenter.init("uid", null, "orgUnit", "stage")
        presenter.scheduleEventPermanent(
            "enrollment", null, "stage", date, "orgUnit", "catCombo", "catOption", geometry
        )

        verify(view).renderError("Error")
    }

    @Test
    fun `Should create an schedule event`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        val date = Date()
        initMocks("uid", null, "stage", catCombo)
        whenever(
            eventInitialRepository.scheduleEvent(
                "enrollment",
                null,
                "uid",
                "stage",
                date,
                "orgUnit",
                "catCombo",
                "catOption",
                geometry
            )
        ) doReturn Observable.just("event")

        presenter.init("uid", null, "orgUnit", "stage")
        presenter.scheduleEvent(
            "enrollment", "stage", date, "orgUnit", "catCombo", "catOption", geometry
        )

        verify(view).onEventCreated("event")
    }

    @Test
    fun `Should how error when there is an problem creating a scheduled event`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        val date = Date()
        initMocks("uid", null, "stage", catCombo)
        whenever(
            eventInitialRepository.scheduleEvent(
                "enrollment",
                null,
                "uid",
                "stage",
                date,
                "orgUnit",
                "catCombo",
                "catOption",
                geometry
            )
        ) doReturn Observable.error(Throwable("Error"))

        presenter.init("uid", null, "orgUnit", "stage")
        presenter.scheduleEvent(
            "enrollment", "stage", date, "orgUnit", "catCombo", "catOption", geometry
        )

        verify(view).renderError("Error")
    }

    @Test
    fun `Should successfully edit event`() {
        val event = Event.builder().uid("event_uid").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        whenever(
            eventInitialRepository.editEvent(
                "tei", "event_uid", "date", "orgUnit", "catCombo", "catOptionCombo", geometry
            )
        ) doReturn Observable.just(event)

        presenter.editEvent(
            "tei",
            "stage",
            "event_uid",
            "date",
            "orgUnit",
            "catCombo",
            "catOptionCombo",
            geometry
        )

        verify(view).onEventUpdated("event_uid")
    }

    @Test
    fun `Should display message when there is a problem editing event`() {
        val event = Event.builder().uid("event_uid").build()
        val geometry = Geometry.builder().type(FeatureType.POINT).build()
        whenever(
            eventInitialRepository.editEvent(
                "tei", "event_uid", "date", "orgUnit", "catCombo", "catOptionCombo", geometry
            )
        ) doReturn Observable.error(Throwable("Error"))

        presenter.editEvent(
            "tei",
            "stage",
            "event_uid",
            "date",
            "orgUnit",
            "catCombo",
            "catOptionCombo",
            geometry
        )

        verify(view).displayMessage("Error")
    }

    @Test
    fun `Should show date dialog`() {
        val listener: DatePickerDialog.OnDateSetListener = mock()
        presenter.onDateClick(listener)
        verify(view).showDateDialog(listener)
    }

    @Test
    fun `Should show orgUnit selector`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        initMocks("uid", null, "stageUid", catCombo, true)

        presenter.init("uid", null, "orgUnit", "stageUid")
        presenter.onOrgUnitButtonClick()
        verify(view).showOrgUnitSelector(any())
    }

    @Test
    fun `Should check fab button visibility on field changes`() {
        presenter.onFieldChanged("", 0, 1, 1)
        verify(view).checkActionButtonVisibility()
    }

    @Test
    fun `Should clear disposable`() {
        val size = presenter.compositeDisposable.size()
        presenter.onDettach()
        assert(size == 0)
    }

    @Test
    fun `Should display message`() {
        presenter.displayMessage("message")
        verify(view).displayMessage("message")
    }

    @Test
    fun `Should get the CategoryOptionCombo uid`() {
        val uid = "uid"
        whenever(eventInitialRepository.getCategoryOptionCombo("catCombo", listOf())) doReturn uid

        val catOptionCombo = presenter.getCatOptionCombo("catCombo", listOf(), listOf())
        assertTrue(uid == catOptionCombo)
    }

    @Test
    fun `Should get the stage last date`() {
        val date = Date()
        whenever(eventInitialRepository.getStageLastDate("stageUid", "enrollment")) doReturn date

        val result = presenter.getStageLastDate("stageUid", "enrollment")
        assertTrue(date == result)
    }

    @Test
    fun `Should get event orgUnit`() {
        val orgUnit = OrganisationUnit.builder().uid("uid").displayName("name").build()
        whenever(
            eventInitialRepository.getOrganisationUnit("uid")
        ) doReturn Observable.just(orgUnit)

        presenter.getEventOrgUnit("uid")
        verify(view).setOrgUnit(orgUnit.uid(), orgUnit.displayName())
    }

    @Test
    fun `Should initialize orgUnit when there is only one orgUnit`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val selectedDate = Date()
        val date = DateUtils.databaseDateFormat().format(selectedDate)

        initMocks("uid", null, "stageUid", catCombo)
        whenever(
            eventInitialRepository.filteredOrgUnits(date, "uid", null)
        ) doReturn Observable.just(listOf())
        whenever(view.eventcreateionType()) doReturn EventCreationType.ADDNEW

        presenter.init("uid", null, "orgUnit", "stageUid")
        presenter.initOrgunit(selectedDate)

        verify(view).setInitialOrgUnit(any())
    }

    @Test
    fun `Should initialize orgUnit when there are more than one orgUnit`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val selectedDate = Date()
        val date = DateUtils.databaseDateFormat().format(selectedDate)

        initMocks("uid", null, "stageUid", catCombo, true)
        whenever(
            eventInitialRepository.filteredOrgUnits(date, "uid", null)
        ) doReturn Observable.just(listOf())
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn true
        whenever(preferences.getString(Preference.CURRENT_ORG_UNIT)) doReturn "orgUnit"

        presenter.init("uid", null, "orgUnit", "stageUid")
        presenter.initOrgunit(selectedDate)

        verify(view).setInitialOrgUnit(any())
    }

    @Test
    fun `Should initialize orgUnits when there are not any orgUnit available`() {
        val catCombo = CategoryCombo.builder().uid("catCombo").build()
        val selectedDate = Date()
        val date = DateUtils.databaseDateFormat().format(selectedDate)

        initMocks("uid", null, "stageUid", catCombo, true)
        whenever(
            eventInitialRepository.filteredOrgUnits(date, "uid", null)
        ) doReturn Observable.error(Throwable("Error"))

        presenter.init("uid", null, "orgUnit", "stageUid")
        presenter.initOrgunit(selectedDate)

        verify(view).setInitialOrgUnit(null)
    }

    @Test
    fun `Should get CategoryOption`() {
        val categoryOption = CategoryOption.builder().uid("uid").build()
        whenever(eventInitialRepository.getCatOption("uid")) doReturn categoryOption

        val result = presenter.getCatOption("uid")
        assert(categoryOption.uid() == result.uid())
    }

    @Test
    fun `Should get the CategoryOptions size`() {
        val size = 2
        whenever(eventInitialRepository.getCatOptionSize("uid")) doReturn size

        val result = presenter.catOptionSize("uid")
        assert(result == size)
    }

    @Test
    fun `Should set changing coordinates`() {
        presenter.setChangingCoordinates(true)
        verify(preferences).setValue(Preference.EVENT_COORDINATE_CHANGED, true)
    }

    @Test
    fun `Should clear changing coordinates`() {
        presenter.setChangingCoordinates(false)
        verify(preferences).removeValue(Preference.EVENT_COORDINATE_CHANGED)
    }

    @Test
    fun `Should get the list of the categoryOptions`() {
        val catOptions = listOf<CategoryOption>(
            CategoryOption.builder().uid("uid1").build(),
            CategoryOption.builder().uid("uid2").build()
        )
        whenever(eventInitialRepository.getCategoryOptions("catUid")) doReturn catOptions

        val result = presenter.getCatOptions("catUid")
        assert(result.size == 2)
        assert(result[1].uid() == "uid2")
    }

    @Test
    fun `Should not show the completion percentage view`() {
        val shouldShow = false
        whenever(eventInitialRepository.showCompletionPercentage()) doReturn shouldShow

        val result = presenter.completionPercentageVisibility
        assert(shouldShow == result)
    }

    @Test
    fun `Should show the completion percentage view`() {
        val shouldShow = true
        whenever(eventInitialRepository.showCompletionPercentage()) doReturn shouldShow

        val result = presenter.completionPercentageVisibility
        assert(shouldShow == result)
    }

    @Test
    fun `Should track event analitycs`() {
        presenter.onEventCreated()

        verify(matomoAnalyticsController).trackEvent(any(), any(), any())
    }

    private fun initMocks(
        uid: String?,
        eventId: String?,
        programStageUid: String?,
        catCombo: CategoryCombo,
        moreOrgUnits: Boolean = false
    ) {
        val program = Program.builder().uid(uid).build()
        val orgUnits =
            mutableListOf(OrganisationUnit.builder().uid("orgUnit").displayName("name").build())
        val programStage = ProgramStage.builder().uid(programStageUid).build()

        if (moreOrgUnits) {
            orgUnits.add(OrganisationUnit.builder().uid("orgUnit2").displayName("name").build())
        }

        whenever(eventInitialRepository.accessDataWrite(uid)) doReturn Observable.just(true)
        whenever(eventInitialRepository.getGeometryModel(uid, null)) doReturn Single.just(
            CoordinateViewModel.create(
                "id", "", false, null, null,
                true, null, ObjectStyle.builder().build(), null, false,
                false, null, null, null
            )
        )
        whenever(eventInitialRepository.getProgramWithId(uid)) doReturn Observable.just(program)
        whenever(eventInitialRepository.catCombo(uid)) doReturn Observable.just(catCombo)
        whenever(eventInitialRepository.orgUnits(uid)) doReturn Observable.just(orgUnits.toList())
        whenever(
            eventInitialRepository.catOptionCombos(catCombo.uid())
        ) doReturn Observable.just(listOf<CategoryOptionCombo>())
        whenever(
            eventInitialRepository.getOrganisationUnit("orgUnit")
        ) doReturn Observable.just(orgUnits[0])

        if (eventId != null) {
            eventMocks(eventId)
        } else {
            whenever(
                eventInitialRepository.programStageWithId(programStageUid)
            ) doReturn Observable.just(programStage)
        }
    }

    private fun eventMocks(eventId: String) {
        val event = Event.builder().uid(eventId).build()
        val programStage = ProgramStage.builder().uid(eventId).build()
        val stringCategoryOptionMap = mutableMapOf<String, CategoryOption>()

        whenever(eventInitialRepository.event(eventId)) doReturn Observable.just(event)
        whenever(
            eventInitialRepository.programStageForEvent(eventId)
        ) doReturn Flowable.just(programStage)
        whenever(
            eventInitialRepository.getOptionsFromCatOptionCombo(eventId)
        ) doReturn Flowable.just(stringCategoryOptionMap)

        whenever(eventInitialRepository.list()) doReturn Flowable.just(listOf())
        whenever(
            eventInitialRepository.calculate()
        ) doReturn Flowable.just(Result.success(listOf()))
        whenever(eventInitialRepository.eventSections()) doReturn Flowable.just(listOf())
        whenever(
            eventFieldMapper.map(
                mutableListOf(),
                mutableListOf(),
                "",
                mutableMapOf(),
                mutableMapOf(),
                false to false
            )
        ) doReturn Pair(mutableListOf(), mutableListOf())
    }
}
