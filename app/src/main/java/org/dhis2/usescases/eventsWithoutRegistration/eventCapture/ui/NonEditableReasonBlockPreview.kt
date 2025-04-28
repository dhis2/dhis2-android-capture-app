package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.commons.ui.NonEditableReasonBlock

@Preview
@Composable
fun NonEditableReasonBlockPreview() {
    NonEditableReasonBlock(
        reason = "This data is not editable because it is marked as completed.",
        canBeReopened = true,
        onReopenClick = {
        },
    )
}
