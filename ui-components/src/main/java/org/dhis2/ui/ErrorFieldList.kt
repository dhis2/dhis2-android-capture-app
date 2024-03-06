package org.dhis2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.ui.dialogs.bottomsheet.IssueItem

@Composable
fun ErrorFieldList(
    fieldsWithIssues: List<FieldWithIssue> = emptyList(),
    onItemClick: (fieldUid: String) -> Unit = {}
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(fieldsWithIssues) {
            IssueItem(it, onClick = { onItemClick(it.fieldUid) })
        }
    }
}
