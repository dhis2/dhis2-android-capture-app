package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.junit.Before
import org.junit.Test

class EventCaptureFormPresenterTest {
    private lateinit var presenter: EventCaptureFormPresenter
    private val activityPresenter: EventCaptureContract.Presenter = mock()
    private val view: EventCaptureFormView = mock()
    private val schedulerProvider = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = EventCaptureFormPresenter(
            view,
            activityPresenter,
            schedulerProvider
        )
    }

    @Test
    fun `Should try to finish`() {
        presenter.onActionButtonClick()
        verify(activityPresenter).attemptFinish()
    }
}
