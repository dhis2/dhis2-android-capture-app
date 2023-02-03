package org.dhis2.utils.granularsync

import android.content.res.Resources
import java.util.Date
import org.dhis2.usescases.sms.InputArguments
import org.dhis2.usescases.sms.SmsSendingService
import org.dhis2.usescases.sms.StatusText

class SyncStatusDialogUiConfig(
    private val resources: Resources,
    private val presenter: GranularSyncPresenter,
    private val inputArguments: InputArguments
) {

    fun initialStatusLogItem(status: SmsSendingService.SendingStatus): StatusLogItem {
        return StatusLogItem.create(
            Date(),
            initialStatusMessage(status)
        )
    }

    private fun initialStatusMessage(status: SmsSendingService.SendingStatus): String {
        return "%s: %s".format(
            formatSubmissionTypeText(StatusText.getTextSubmissionType(resources, inputArguments)),
            StatusText.getTextForStatus(resources, status)
        )
    }

    private fun formatSubmissionTypeText(defaultText: String): String {
        return if (isEnrollmentSubmission()) {
            val trackedEntityTypeName =
                presenter.trackedEntityTypeNameFromEnrollment(inputArguments.enrollmentId)
            trackedEntityTypeName?.let {
                defaultText.replace("TEI", trackedEntityTypeName)
            } ?: defaultText
        } else {
            defaultText
        }
    }

    private fun isEnrollmentSubmission() =
        inputArguments.submissionType == InputArguments.Type.ENROLLMENT &&
            inputArguments.enrollmentId != null
}
