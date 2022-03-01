package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.junit.Before
import org.junit.Test

class EventCaptureFormPresenterTest {
    private lateinit var presenter: EventCaptureFormPresenter
    private val activityPresenter: EventCaptureContract.Presenter = mock()

    @Before
    fun setUp() {
        presenter = EventCaptureFormPresenter(activityPresenter)
    }

    @Test
    fun `Should try to finish with fields with errors`() {
        presenter.handleDataIntegrityResult(
            FieldsWithErrorResult(listOf("field1"), false, null, false)
        )
        verify(activityPresenter).attemptFinish(false, null, listOf("field1"), emptyMap())
    }

    @Test
    fun `Should try to finish with fields with warning`() {
        presenter.handleDataIntegrityResult(
            FieldsWithWarningResult(listOf("field1"), true, null)
        )
        verify(activityPresenter).attemptFinish(true, null, emptyList(), emptyMap())
    }

    @Test
    fun `Should try to finish with empty mandatory fields`() {
        presenter.handleDataIntegrityResult(
            MissingMandatoryResult(mapOf(Pair("field1", "section")), false, null, false)
        )
        verify(activityPresenter).attemptFinish(
            false,
            null,
            emptyList(),
            mapOf(Pair("field1", "section"))
        )
    }

    @Test
    fun `Should try to finish  successfully`() {
        presenter.handleDataIntegrityResult(
            SuccessfulResult(null, true, null)
        )
        verify(activityPresenter).attemptFinish(true, null, emptyList(), emptyMap())
    }
}
