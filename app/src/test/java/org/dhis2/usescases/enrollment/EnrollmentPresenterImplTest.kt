package org.dhis2.usescases.enrollment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.forms.dataentry.EnrollmentRepository
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class EnrollmentPresenterImplTest {

    private val enrollmentFormRepository: EnrollmentFormRepository = mock()
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program> = mock()
    private val teiRepository: TrackedEntityInstanceObjectRepository = mock()
    private val dataEntryRepository: EnrollmentRepository = mock()
    lateinit var presenter: EnrollmentPresenterImpl
    private val enrollmentView: EnrollmentView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val valueStore: ValueStore = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val sectionProcessor: FlowableProcessor<String> = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()

    @Before
    fun setUp() {
        presenter = EnrollmentPresenterImpl(
            enrollmentView,
            d2,
            enrollmentRepository,
            dataEntryRepository,
            teiRepository,
            programRepository,
            schedulers,
            enrollmentFormRepository,
            valueStore,
            analyticsHelper,
            matomoAnalyticsController
        )
    }

    @Test
    fun `Open initial when needsCatCombo is false and needsCoordinates is false`() {
        checkCatCombo(true, FeatureType.NONE)
        assert(!presenter.openInitial(""))
    }

    @Test
    fun `Open initial when needsCatCombo is true and needsCoordinates is false`() {
        checkCatCombo(false, FeatureType.NONE)
        assert(presenter.openInitial(""))
    }

    @Test
    fun `Open initial when needsCatCombo is false and needsCoordinates is true`() {
        checkCatCombo(true, FeatureType.POINT)
        assert(presenter.openInitial(""))
    }

    @Test
    fun `Open initial when needsCatCombo is true and needsCoordinates is true`() {
        checkCatCombo(false, FeatureType.POINT)
        assert(presenter.openInitial(""))
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is granted`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .access(
                Access.builder()
                    .data(
                        DataAccess.builder().write(true)
                            .build()
                    ).build()
            ).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentRepository).setStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentView).renderStatus(EnrollmentStatus.ACTIVE)
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is denied`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .access(
                Access.builder()
                    .data(
                        DataAccess.builder().write(false)
                            .build()
                    ).build()
            ).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)

        verify(enrollmentView).displayMessage(null)
    }

    @Test
    fun `Save file should use valueStore`() {
        whenever(valueStore.save("uid", "fileValue")) doReturn Flowable.just(
            StoreResult(
                "uid",
                ValueStoreResult.VALUE_CHANGED
            )
        )
        presenter.saveFile("uid", "fileValue")
        verify(valueStore, times(1)).save("uid", "fileValue")
    }

    @Test
    fun `Should update the fields flowable`() {
        val processor = PublishProcessor.create<Boolean>()
        val testSubscriber = processor.test()

        presenter.updateFields()
        processor.onNext(true)

        testSubscriber.assertValueAt(0, true)
    }

    @Test
    fun `Should execute the backButton processor`() {
        val processor = PublishProcessor.create<Boolean>()
        val testSubscriber = processor.test()

        presenter.backIsClicked()
        processor.onNext(true)

        testSubscriber.assertValueAt(0, true)
    }

    @Test
    fun `Should show a profile picture image`() {
        val path = "route/image"
        whenever(enrollmentFormRepository.getProfilePicture()) doReturn path
        presenter.onTeiImageHeaderClick()
        verify(enrollmentView).displayTeiPicture(path)
    }

    @Test
    fun `Should not show a profile picture image`() {
        val path = ""
        whenever(enrollmentFormRepository.getProfilePicture()) doReturn path
        presenter.onTeiImageHeaderClick()
        verify(enrollmentView, never()).displayTeiPicture(path)
    }

    @Test
    fun `Should show save button when the enrollment is editable`() {
        val geometry = Geometry.builder()
            .coordinates("[-30.00, 11.00]")
            .type(FeatureType.POINT)
            .build()
        val tei = TrackedEntityInstance.builder().geometry(geometry).uid("random").build()
        val program = Program.builder().uid("tUID").build()

        whenever(teiRepository.blockingGet()) doReturn tei
        whenever(programRepository.blockingGet()) doReturn program
        whenever(d2.enrollmentModule()) doReturn mock()
        whenever(d2.enrollmentModule().enrollmentService()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollmentService()
                .blockingGetEnrollmentAccess(tei.uid(), program.uid())
        ) doReturn EnrollmentAccess.WRITE_ACCESS

        presenter.showOrHideSaveButton()

        verify(enrollmentView).setSaveButtonVisible(true)
    }

    @Test
    fun `Should hide save button when the enrollment is not editable`() {
        val geometry = Geometry.builder()
            .coordinates("[-30.00, 11.00]")
            .type(FeatureType.POINT)
            .build()
        val tei = TrackedEntityInstance.builder().geometry(geometry).uid("random").build()
        val program = Program.builder().uid("tUID").build()

        whenever(teiRepository.blockingGet()) doReturn tei
        whenever(programRepository.blockingGet()) doReturn program
        whenever(d2.enrollmentModule()) doReturn mock()
        whenever(d2.enrollmentModule().enrollmentService()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollmentService()
                .blockingGetEnrollmentAccess(tei.uid(), program.uid())
        ) doReturn EnrollmentAccess.NO_ACCESS

        presenter.showOrHideSaveButton()

        verify(enrollmentView).setSaveButtonVisible(false)
    }

    private fun checkCatCombo(catCombo: Boolean, featureType: FeatureType) {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .categoryCombo(ObjectWithUid.create("")).build()

        whenever(d2.eventModule()) doReturn mock()
        whenever(d2.eventModule().events()) doReturn mock()
        whenever(d2.eventModule().events().uid("")) doReturn mock()
        whenever(d2.eventModule().events().uid("").blockingGet()) doReturn Event.builder()
            .uid("").programStage("").build()

        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().uid("")) doReturn mock()
        whenever(
            d2.programModule().programStages().uid("").blockingGet()
        ) doReturn ProgramStage.builder().uid("").featureType(featureType).build()

        whenever(d2.categoryModule()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryCombos().uid("")) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().uid("").blockingGet()
        ) doReturn CategoryCombo.builder()
            .isDefault(catCombo)
            .uid("")
            .build()
    }

    private fun dummyEditTextViewModel(
        uid: String,
        label: String,
        value: String? = null,
        mandatory: Boolean = false
    ) =
        FieldUiModelImpl(
            uid = uid,
            layoutId = 1,
            value = value,
            mandatory = mandatory,
            label = label,
            programStageSection = "testSection",
            valueType = ValueType.TEXT
        )

    private fun mockTrackedEntityAttributes() {
        whenever(d2.trackedEntityModule()) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityAttributes()) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid("uid1")) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid("uid1").blockingGet()
        ) doReturn mock()
    }

    private fun mockTrackedEntityAttributeValues() {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().byTrackedEntityAttribute()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid1")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid1")
                .byValue()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid1")
                .byValue().eq("value")
        ) doReturn mock()
    }
}
