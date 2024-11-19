package org.dhis2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.ui.dialogs.bottomsheet.IssueItem

@Composable
fun ErrorFieldList(
    fieldsWithIssues: List<FieldWithIssue> = emptyList(),
    onItemClick: (fieldUid: String) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        for (field in fieldsWithIssues) {
            IssueItem(field, onClick = { onItemClick(field.fieldUid) })
        }
    }
}
