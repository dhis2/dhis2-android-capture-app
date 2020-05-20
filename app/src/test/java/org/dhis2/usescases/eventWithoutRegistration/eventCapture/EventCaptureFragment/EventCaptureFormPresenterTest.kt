package org.dhis2.usescases.eventWithoutRegistration.eventCapture.EventCaptureFragment

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.BehaviorSubject
import org.dhis2.data.forms.dataentry.StoreResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormPresenter
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormView
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times

class EventCaptureFormPresenterTest {
    private lateinit var presenter: EventCaptureFormPresenter
    private val activityPresenter: EventCaptureContract.Presenter = mock()
    private val view: EventCaptureFormView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val valueStore: ValueStore = mock()
    private val schedulerProvider = TrampolineSchedulerProvider()
    private val processor: FlowableProcessor<String> = PublishProcessor.create()

    @Before
    fun setUp() {
        presenter = EventCaptureFormPresenter(
            view,
            activityPresenter,
            d2,
            valueStore,
            schedulerProvider
        )
    }

    @Test
    fun `Should listen to data entry, sections and field changes`() {
        whenever(view.dataEntryFlowable()) doReturn mock()
        whenever(view.dataEntryFlowable().onBackpressureBuffer()) doReturn mock()
        whenever(
            view.dataEntryFlowable().onBackpressureBuffer().distinctUntilChanged()
        ) doReturn Flowable.just(RowAction.create("", ""))
        whenever(view.sectionSelectorFlowable()) doReturn processor
        whenever(activityPresenter.formFieldsFlowable()) doReturn BehaviorSubject.create()
        presenter.init()

        assert(
            view.dataEntryFlowable()
                .onBackpressureBuffer()
                .distinctUntilChanged()
                .test()
                .hasSubscription()
        )
        assert(view.sectionSelectorFlowable().distinctUntilChanged().test().hasSubscription())
        assert(activityPresenter.formFieldsFlowable().hasObservers())
    }

    @Test
    fun `Should save new value`() {
        whenever(view.dataEntryFlowable()) doReturn mock()
        whenever(view.dataEntryFlowable().onBackpressureBuffer()) doReturn mock()
        whenever(
            view.dataEntryFlowable().onBackpressureBuffer().distinctUntilChanged()
        ) doReturn Flowable.just(RowAction.create("testUid", "testValue"))
        whenever(view.sectionSelectorFlowable()) doReturn processor
        whenever(activityPresenter.formFieldsFlowable()) doReturn BehaviorSubject.create()
        presenter.init()

        verify(valueStore, times(1)).save("testUid", "testValue")
    }

    @Test
    fun `Should ask for new calculation if value saved changed`() {
        whenever(view.dataEntryFlowable()) doReturn mock()
        whenever(view.dataEntryFlowable().onBackpressureBuffer()) doReturn mock()
        whenever(
            view.dataEntryFlowable().onBackpressureBuffer().distinctUntilChanged()
        ) doReturn Flowable.just(RowAction.create("testUid", "testValue"))
        whenever(view.sectionSelectorFlowable()) doReturn processor
        whenever(activityPresenter.formFieldsFlowable()) doReturn BehaviorSubject.create()
        whenever(valueStore.save("testUid", "testValue")) doReturn Flowable.just(
            StoreResult("testUid", ValueStoreImpl.ValueStoreResult.VALUE_CHANGED)
        )
        presenter.init()

        verify(activityPresenter, times(1)).setLastUpdatedUid("")
        verify(activityPresenter, times(1)).nextCalculation(true)
    }

    @Test
    fun `Should not ask for new calculation if value saved did not changed`() {
        whenever(view.dataEntryFlowable()) doReturn mock()
        whenever(view.dataEntryFlowable().onBackpressureBuffer()) doReturn mock()
        whenever(
            view.dataEntryFlowable().onBackpressureBuffer().distinctUntilChanged()
        ) doReturn Flowable.just(RowAction.create("testUid", "testValue"))
        whenever(view.sectionSelectorFlowable()) doReturn processor
        whenever(activityPresenter.formFieldsFlowable()) doReturn BehaviorSubject.create()
        whenever(valueStore.save("testUid", "testValue")) doReturn Flowable.just(
            StoreResult("testUid", ValueStoreImpl.ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        )
        presenter.init()

        verify(activityPresenter, times(0)).setLastUpdatedUid("testUid")
        verify(activityPresenter, times(0)).nextCalculation(true)
    }

    @Test
    fun `Should go to new section`() {
        whenever(view.dataEntryFlowable()) doReturn mock()
        whenever(view.dataEntryFlowable().onBackpressureBuffer()) doReturn mock()
        whenever(
            view.dataEntryFlowable().onBackpressureBuffer().distinctUntilChanged()
        ) doReturn Flowable.just(RowAction.create("", ""))
        whenever(view.sectionSelectorFlowable()) doReturn processor
        whenever(activityPresenter.formFieldsFlowable()) doReturn BehaviorSubject.create()
        presenter.init()
        processor.onNext("sectionUid")
        verify(activityPresenter, times(1)).goToSection("sectionUid")
    }

    @Test
    fun `Should show fields`() {
        whenever(view.dataEntryFlowable()) doReturn mock()
        whenever(view.dataEntryFlowable().onBackpressureBuffer()) doReturn mock()
        whenever(
            view.dataEntryFlowable().onBackpressureBuffer().distinctUntilChanged()
        ) doReturn Flowable.just(RowAction.create("", ""))
        whenever(view.sectionSelectorFlowable()) doReturn processor
        whenever(activityPresenter.formFieldsFlowable()) doReturn BehaviorSubject.create()
        presenter.init()
        activityPresenter.formFieldsFlowable().onNext(mutableListOf())
        verify(view, times(1)).showFields(any(), any())
    }

    @Test
    fun `Should clear disposable`() {
        whenever(view.dataEntryFlowable()) doReturn mock()
        whenever(view.dataEntryFlowable().onBackpressureBuffer()) doReturn mock()
        whenever(
            view.dataEntryFlowable().onBackpressureBuffer().distinctUntilChanged()
        ) doReturn Flowable.just(RowAction.create("", ""))
        whenever(view.sectionSelectorFlowable()) doReturn processor
        whenever(activityPresenter.formFieldsFlowable()) doReturn BehaviorSubject.create()
        presenter.init()
        presenter.onDetach()
        assert(presenter.disposable.size() == 0)
    }

    @Test
    fun `Should try to finish`() {
        presenter.onActionButtonClick()
        verify(activityPresenter).attempFinish()
    }
}
