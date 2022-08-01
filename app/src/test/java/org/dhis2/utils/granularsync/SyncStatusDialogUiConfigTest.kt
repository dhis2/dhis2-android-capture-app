package org.dhis2.utils.granularsync

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.R
import org.dhis2.usescases.sms.InputArguments
import org.dhis2.usescases.sms.SmsSendingService
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncStatusDialogUiConfigTest {
    private val resources: Resources = mock()
    private val presenter: GranularSyncContracts.Presenter = mock()
    private val inputArguments: InputArguments = mock()
    private val config = SyncStatusDialogUiConfig(resources, presenter, inputArguments)

    @Test
    fun shouldReplaceTEItermWithTrackedEntityType() {
        val enrollmentTitleMessage = "SMS TEI registration and enrollment"
        val expectedMessage = "SMS Person registration and enrollment: Started sms submission"
        val status = SmsSendingService.SendingStatus(
            1,
            SmsSendingService.State.STARTED,
            null,
            0,
            1
        )
        whenever(inputArguments.submissionType) doReturn InputArguments.Type.ENROLLMENT
        whenever(inputArguments.enrollmentId) doReturn "enrollmentUid"
        whenever(presenter.trackedEntityTypeNameFromEnrollment("enrollmentUid")) doReturn "Person"
        whenever(resources.getString(R.string.sms_title_enrollment))doReturn enrollmentTitleMessage
        whenever(resources.getString(R.string.sms_state_started)) doReturn "Started sms submission"
        assertTrue(config.initialStatusLogItem(status).description() == expectedMessage)
    }

    @Test
    fun shouldNotReplaceTEItermIfTrackedEntityTypeIsNull() {
        val enrollmentTitleMessage = "SMS TEI registration and enrollment"
        val expectedMessage = "SMS TEI registration and enrollment: Started sms submission"
        val status = SmsSendingService.SendingStatus(
            1,
            SmsSendingService.State.STARTED,
            null,
            0,
            1
        )
        whenever(inputArguments.submissionType) doReturn InputArguments.Type.ENROLLMENT
        whenever(inputArguments.enrollmentId) doReturn "enrollmentUid"
        whenever(presenter.trackedEntityTypeNameFromEnrollment("enrollmentUid")) doReturn null
        whenever(resources.getString(R.string.sms_title_enrollment))doReturn enrollmentTitleMessage
        whenever(resources.getString(R.string.sms_state_started)) doReturn "Started sms submission"
        assertTrue(config.initialStatusLogItem(status).description() == expectedMessage)
    }

    @Test
    fun shouldNotReplaceTEIterm() {
        val enrollmentTitleMessage = "SMS data set submission"
        val expectedMessage = "SMS data set submission: Started sms submission"
        val status = SmsSendingService.SendingStatus(
            1,
            SmsSendingService.State.STARTED,
            null,
            0,
            1
        )
        whenever(inputArguments.submissionType) doReturn InputArguments.Type.DATA_SET
        whenever(resources.getString(R.string.sms_title_data_set))doReturn enrollmentTitleMessage
        whenever(resources.getString(R.string.sms_state_started)) doReturn "Started sms submission"
        assertTrue(config.initialStatusLogItem(status).description() == expectedMessage)
    }
}
