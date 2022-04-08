package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.dhis2.commons.data.FieldWithIssue
import org.dhis2.commons.data.IssueType
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.junit.Before
import org.junit.Test

class EventCaptureFormPresenterTest {
    private lateinit var presenter: EventCaptureFormPresenter
    private val activityPresenter: EventCaptureContract.Presenter = mock()
    private val view: EventCaptureFormView = mock()
    private val d2: D2 = mock()
    private val eventUid: String = "random_ID"

    @Before
    fun setUp() {
        presenter = EventCaptureFormPresenter(view, activityPresenter, d2, eventUid)
    }

    @Test
    fun `Should try to finish with fields with errors`() {
        val errorFields = listOf(
            FieldWithIssue(
                "Uid",
                "field1",
                IssueType.ERROR,
                "message"
            )
        )
        presenter.handleDataIntegrityResult(
            FieldsWithErrorResult(
                emptyMap(),
                errorFields,
                emptyList(),
                false,
                null,
                false
            )
        )
        verify(activityPresenter).attemptFinish(
            false,
            null,
            errorFields,
            emptyMap(),
            emptyList()
        )
    }

    @Test
    fun `Should try to finish with fields with warning`() {
        val warningFields = listOf(
            FieldWithIssue(
                "Uid",
                "field1",
                IssueType.WARNING,
                "message"
            )
        )
        presenter.handleDataIntegrityResult(
            FieldsWithWarningResult(
                warningFields,
                true,
                null
            )
        )
        verify(activityPresenter).attemptFinish(
            true,
            null,
            emptyList(),
            emptyMap(),
            warningFields
        )
    }

    @Test
    fun `Should try to finish with empty mandatory fields`() {
        presenter.handleDataIntegrityResult(
            MissingMandatoryResult(
                mapOf(Pair("field1", "section")),
                emptyList(),
                emptyList(),
                false,
                null,
                false
            )
        )
        verify(activityPresenter).attemptFinish(
            false,
            null,
            emptyList(),
            mapOf(Pair("field1", "section")),
            emptyList()
        )
    }

    @Test
    fun `Should try to finish  successfully`() {
        presenter.handleDataIntegrityResult(
            SuccessfulResult(null, true, null)
        )
        verify(activityPresenter).attemptFinish(true, null, emptyList(), emptyMap(), emptyList())
    }

    @Test
    fun `Should show save button when event is editable`() {
        val editableStatus = EventEditableStatus.Editable()
        whenever(d2.eventModule()) doReturn mock()
        whenever(d2.eventModule().eventService()) doReturn mock()
        whenever(d2.eventModule().eventService().getEditableStatus(eventUid)) doReturn Single.just(
            editableStatus
        )

        presenter.showOrHideSaveButton()

        verify(view).showSaveButton()
    }

    @Test
    fun `Should hide save button when event is not editable`() {
        val editableStatus =
            EventEditableStatus.NonEditable(EventNonEditableReason.BLOCKED_BY_COMPLETION)
        whenever(d2.eventModule()) doReturn mock()
        whenever(d2.eventModule().eventService()) doReturn mock()
        whenever(d2.eventModule().eventService().getEditableStatus(eventUid)) doReturn Single.just(
            editableStatus
        )

        presenter.showOrHideSaveButton()

        verify(view).hideSaveButton()
    }
}
