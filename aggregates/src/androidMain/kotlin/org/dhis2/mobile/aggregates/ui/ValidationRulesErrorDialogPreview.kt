package org.dhis2.mobile.aggregates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.aggregates.ui.component.ValidationBar
import org.dhis2.mobile.aggregates.ui.component.ValidationRulesErrorDialog
import org.dhis2.mobile.aggregates.ui.states.ValidationBarUiState

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ValidationRulesErrorDialogPreview() {
    ValidationRulesErrorDialog()
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ValidationBarPreview() {
    ValidationBar(
        ValidationBarUiState(
            quantity = 1,
            description = "description",
            onExpandErrors = {},
        ),
    )
}
