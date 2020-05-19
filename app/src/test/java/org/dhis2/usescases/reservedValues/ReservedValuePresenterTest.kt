package org.dhis2.usescases.reservedValues

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.reservedValue.ReservedValueContracts
import org.dhis2.usescases.reservedValue.ReservedValuePresenter
import org.dhis2.usescases.reservedValue.ReservedValueRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.trackedentity.ReservedValueSummary
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.junit.Before
import org.junit.Test

class ReservedValuePresenterTest {

    private lateinit var reservedValuePresenter: ReservedValuePresenter
    private val repository: ReservedValueRepository = mock()
    private val dummyD2Progress = dummyD2Progress()
    private val d2: D2 = mock {
        on { trackedEntityModule() } doReturn mock()
        on { trackedEntityModule().reservedValueManager() } doReturn mock()
    }
    private val view: ReservedValueContracts.View = mock()
    private val scheduler = TrampolineSchedulerProvider()

    @Before
    fun setup() {
        reservedValuePresenter = ReservedValuePresenter(repository, d2, scheduler, view)
    }

    @Test
    fun `Should init sucessfully and show reserved values`() {
        val dataElements = dummyDataElements()
        whenever(repository.reservedValues) doReturn dummyDataElements()

        reservedValuePresenter.init()

        verify(view).setReservedValues(dataElements?.blockingGet())
    }

    @Test
    fun `Should download reserved values when refill is clicked`() {
        whenever(
            d2.trackedEntityModule().reservedValueManager().downloadReservedValues(
                "attr",
                100
            )
        ) doReturn dummyD2Progress

        reservedValuePresenter.onClickRefill(dummyReservedValueModel())

        assert(!dummyD2Progress.isEmpty.blockingGet())
        verifyZeroInteractions(view)
    }

    @Test
    fun `Should catch exception D2 when error happens during download`() {
        whenever(
            d2.trackedEntityModule().reservedValueManager().downloadReservedValues(
                "attr",
                100
            )
        ) doReturn D2Error()

        reservedValuePresenter.onClickRefill(dummyReservedValueModel())

        verify(view).showReservedValuesError()
    }

    @Test
    fun `Should not catch exception random when error happens during download`() {
        whenever(
            d2.trackedEntityModule().reservedValueManager().downloadReservedValues(
                "attr",
                100
            )
        ) doReturn Observable.error(Throwable("random"))

        reservedValuePresenter.onClickRefill(dummyReservedValueModel())

        verifyZeroInteractions(view)
    }

    @Test
    fun `Should click on back view`() {
        reservedValuePresenter.onBackClick()

        verify(view).onBackClick()
    }

    @Test
    fun `Should clear disposable`() {
        reservedValuePresenter.onPause()

        assert(reservedValuePresenter.disposable.size() == 0)
    }

    private fun dummyD2Progress() =
        Observable.just(
            D2Progress.builder().totalCalls(5).doneCalls(listOf("1"))
                .isComplete(false).build()
        )

    private fun dummyReservedValueModel() =
        ReservedValueSummary.create(
            TrackedEntityAttribute.builder()
                .uid("attr")
                .displayName("attrName")
                .build(),
            OrganisationUnit.builder()
                .uid("org")
                .displayName("orgUnitName")
                .build(),
            3
        )

    private fun D2Error(): Observable<D2Progress> {
        return Observable.error(
            D2Error.builder().httpErrorCode(500)
                .errorCode(D2ErrorCode.API_RESPONSE_PROCESS_ERROR)
                .errorComponent(
                    D2ErrorComponent.Database
                ).errorDescription("buug").build()
        )
    }

    private fun dummyDataElements(): Single<MutableList<ReservedValueSummary>>? {
        val reservedValue =
            ReservedValueSummary.create(
                TrackedEntityAttribute.builder()
                    .uid("attr")
                    .displayName("attrName")
                    .build(),
                OrganisationUnit.builder()
                    .uid("org")
                    .displayName("orgUnitName")
                    .build(),
                3
            )
        return Single.just(mutableListOf(reservedValue))
    }
}
