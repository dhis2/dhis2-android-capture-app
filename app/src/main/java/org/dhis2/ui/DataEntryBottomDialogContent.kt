package org.dhis2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.commons.data.FieldWithIssue
import org.dhis2.commons.data.IssueType

@Composable
fun DataEntryBottomDialogContent(
    isSaved: Boolean,
    message: String,
    fieldsWithIssues: List<FieldWithIssue>? = emptyList(),
    mainButtonContent: @Composable RowScope.() -> Unit,
    onMainButtonClicked: () -> Unit,
    secondaryButtonContent: @Composable RowScope.() -> Unit = {},
    onSecondaryButtonClicked: () -> Unit = {}
) {
    val modifier = Modifier
        .padding(24.dp)
        .fillMaxWidth()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(28.dp, 28.dp))
    ) {
        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = modifier
        ) {
            Icon(
                painter = painterResource(
                    when {
                        isSaved ->
                            R.drawable.ic_saved_check
                        fieldsWithIssues?.any { it.issueType == IssueType.ERROR } == true ->
                            R.drawable.ic_error_outline
                        else ->
                            R.drawable.ic_alert
                    }
                ),
                contentDescription = "",
                tint = Color.Unspecified
            )
            Text(
                text = when {
                    isSaved -> stringResource(R.string.saved)
                    else -> stringResource(R.string.not_saved)
                },
                style = MaterialTheme.typography.h5,
                color = colorResource(id = R.color.textPrimary),
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.body2,
                color = colorResource(id = R.color.textSecondary)
            )
        }
        Divider(Modifier.padding(horizontal = 24.dp))
        if (fieldsWithIssues?.isNotEmpty() == true) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier
            ) {
                items(fieldsWithIssues) { IssueItem(it) }
            }
            Divider(Modifier.padding(horizontal = 24.dp))
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                elevation = ButtonDefaults.elevation(0.dp),
                onClick = { onSecondaryButtonClicked() },
                content = secondaryButtonContent
            )
            Button(
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.colorPrimary),
                    contentColor = Color.White
                ),
                onClick = { onMainButtonClicked() },
                content = mainButtonContent
            )
        }
    }
}

@Composable
fun IssueItem(fieldWithIssue: FieldWithIssue) {
    Row {
        Icon(
            painter = painterResource(
                when (fieldWithIssue.issueType) {
                    IssueType.ERROR -> R.drawable.ic_error_outline
                    else -> R.drawable.ic_alert
                }
            ),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier
                .width(20.dp)
                .height(20.dp)
        )
        Column(Modifier.padding(start = 11.dp)) {
            Text(text = fieldWithIssue.fieldName, color = colorResource(id = R.color.textPrimary))
            Text(text = fieldWithIssue.message, color = colorResource(id = R.color.textSecondary))
        }
    }
}

@Preview
@Composable
fun DialogPreview1() {
    DataEntryBottomDialogContent(
        isSaved = false,
        message = "If you exit now all the information in the form will be discarded.",
        mainButtonContent = { Text(text = "Keep editing") },
        onMainButtonClicked = {},
        secondaryButtonContent = {
            Text(text = "Discard changes", color = colorResource(id = R.color.warning_color))
        },
        onSecondaryButtonClicked = {}
    )
}

@Preview
@Composable
fun DialogPreview2() {
    DataEntryBottomDialogContent(
        isSaved = true,
        message = "Do you want to mark this form as complete?",
        mainButtonContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_event_status_complete),
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(text = "Complete")
        },
        onMainButtonClicked = {},
        secondaryButtonContent = {
            Text(text = "Not now", color = colorResource(id = R.color.colorPrimary))
        },
        onSecondaryButtonClicked = {}
    )
}

@Preview
@Composable
fun DialogPreview3() {
    DataEntryBottomDialogContent(
        isSaved = false,
        message = "Some mandatory fields are missing and the form cannot be saved.",
        fieldsWithIssues = listOf(
            FieldWithIssue("Age", IssueType.MANDATORY, "Field Mandatory")
        ),
        mainButtonContent = { Text(text = "Review") },
        onMainButtonClicked = {}
    )
}

@Preview
@Composable
fun DialogPreview4() {
    DataEntryBottomDialogContent(
        isSaved = false,
        message = "Some fields have errors and they are not saved." +
            "If you exit now the changes will be discarded.",
        fieldsWithIssues = listOf(
            FieldWithIssue("Age", IssueType.ERROR, "Enter text"),
            FieldWithIssue("Date of birth", IssueType.ERROR, "Enter text")
        ),
        mainButtonContent = { Text(text = "Review") },
        onMainButtonClicked = {}
    )
}
