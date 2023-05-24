package org.dhis2.usescases.reservedValues

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.reservedValue.ReservedValueModel
import org.dhis2.usescases.reservedValue.ReservedValuePresenter
import org.dhis2.usescases.reservedValue.ReservedValueRepository
import org.dhis2.usescases.reservedValue.ReservedValueView
import org.hisp.dhis.android.core.arch.call.BaseD2Progress
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.junit.Before
import org.junit.Test

class ReservedValuePresenterTest {

    private lateinit var reservedValuePresenter: ReservedValuePresenter
    private val repository: ReservedValueRepository = mock()
    private val dummyD2Progress = dummyD2Progress()
    private val view: ReservedValueView = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private val refillFlowable: FlowableProcessor<String> = PublishProcessor.create()

    @Before
    fun setup() {
        reservedValuePresenter = ReservedValuePresenter(repository, scheduler, view, refillFlowable)
    }

    @Test
    fun `Should init sucessfully and show reserved values`() {
        whenever(repository.reservedValues()) doReturn mockedReservedValues()
        reservedValuePresenter.init()
        verify(view).setReservedValues(any())
    }

    @Test
    fun `Should download reserved values when refill is clicked`() {
        whenever(repository.refillReservedValues("attr")) doReturn dummyD2Progress
        reservedValuePresenter.init()
        refillFlowable.onNext("attr")

        assert(!dummyD2Progress.isEmpty.blockingGet())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should catch exception D2 when error happens during download`() {
        whenever(repository.refillReservedValues("attr")) doReturn D2Error()

        reservedValuePresenter.init()
        refillFlowable.onNext("attr")

        verify(view).showReservedValuesError()
    }

    @Test
    fun `Should not catch exception random when error happens during download`() {
        whenever(
            repository.refillReservedValues("attr")
        ) doReturn Observable.error(Throwable("random"))

        reservedValuePresenter.init()
        refillFlowable.onNext("attr")

        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should click on back view`() {
        reservedValuePresenter.onBackClick()

        verify(view).onBackClick()
    }

    private fun dummyD2Progress(): Observable<D2Progress> = Observable.just(
        BaseD2Progress.builder().totalCalls(5).doneCalls(listOf("1"))
            .isComplete(false).build()
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

    private fun mockedReservedValues(): Single<List<ReservedValueModel>> {
        val reservedValue =
            ReservedValueModel(
                "attr",
                "attrName",
                "org",
                "orgUnitName",
                3,
                "%d values left",
                refillFlowable
            )
        return Single.just(mutableListOf(reservedValue))
    }
}
