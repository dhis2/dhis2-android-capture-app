package org.dhis2.usescases.enrollment

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.forms.dataentry.DataEntryRepository
import org.dhis2.data.forms.dataentry.StoreResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class EnrollmentPresenterImplTest {

    private val formRepository: EnrollmentFormRepository = mock()
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program> = mock()
    private val teiRepository: TrackedEntityInstanceObjectRepository = mock()
    private val dataEntryRepository: DataEntryRepository = mock()
    lateinit var presenter: EnrollmentPresenterImpl
    private val enrollmentView: EnrollmentView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val valueStore: ValueStore = mock()
    private val analyticsHelper: AnalyticsHelper = mock()

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
            formRepository,
            valueStore,
            analyticsHelper
        )
    }

    @Test
    fun `Missing and errors fields should show mandatory fields dialog`() {

        getEmptyAndErrorFields(
            showMandatory = true,
            showError = true
        ).forEach { presenter.fieldIntegrityCheck(it) }

        val checkWthErrors = presenter.dataIntegrityCheck()

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage(any())
    }

    @Test
    fun `Missing fields should show mandatory fields dialog`() {
        getEmptyAndErrorFields(
            showMandatory = true,
            showError = false
        ).forEach { presenter.fieldIntegrityCheck(it) }
        val checkWthErrors = presenter.dataIntegrityCheck()

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage(any())
    }

    @Test
    fun `Error fields should show mandatory fields dialog`() {
        getEmptyAndErrorFields(
            showMandatory = false,
            showError = true
        ).forEach { presenter.fieldIntegrityCheck(it) }
        val checkWthErrors = presenter.dataIntegrityCheck()

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showErrorFieldsMessage(any())
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
                ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
            )
        )
        presenter.saveFile("uid", "fileValue")
        verify(valueStore, times(1)).save("uid", "fileValue")
    }

    @Test
    fun `Check data integrity when mandatory is true and has error is false`() {
        val result = presenter.dataIntegrityCheck()
        verifyZeroInteractions(enrollmentView)
        Assert.assertTrue(result)
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

    fun getEmptyAndErrorFields(showMandatory: Boolean, showError: Boolean): List<FieldViewModel> {
        val list = mutableListOf<FieldViewModel>()
        if (showMandatory) {
            list.add(
                EditTextViewModel.create(
                    "field1",
                    "field1",
                    true,
                    null,
                    "hint",
                    1,
                    ValueType.TEXT,
                    null,
                    true,
                    null,
                    null,
                    ObjectStyle.builder().build(),
                    null
                )
            )
        }
        if (showError) {
            list.add(EditTextViewModel.create(
                "field1",
                "field2",
                false,
                null,
                "hint",
                1,
                ValueType.TEXT,
                null,
                true,
                null,
                null,
                ObjectStyle.builder().build(),
                null
            ).withError("error"))
        }
        return list
    }
}
