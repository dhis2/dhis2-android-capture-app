package org.dhis2.commons.dialogs.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.commons.R
import org.dhis2.commons.data.FieldWithIssue
import org.dhis2.commons.data.IssueType

@Composable
fun BottomSheetDialogContent(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    onMainButtonClicked: () -> Unit,
    onSecondaryButtonClicked: () -> Unit = {},
    onIssueItemClicked: () -> Unit = {}
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
            modifier = modifier.weight(1f, false)
        ) {
            Icon(
                painter = painterResource(bottomSheetDialogUiModel.iconResource),
                contentDescription = "",
                tint = Color.Unspecified
            )
            Text(
                text = bottomSheetDialogUiModel.title,
                style = MaterialTheme.typography.h5,
                color = colorResource(id = R.color.textPrimary),
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = bottomSheetDialogUiModel.subtitle,
                style = MaterialTheme.typography.body2,
                color = colorResource(id = R.color.textSecondary),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Divider(Modifier.padding(horizontal = 24.dp))
        if (bottomSheetDialogUiModel.fieldsWithIssues.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier.weight(1f, false)
            ) {
                items(bottomSheetDialogUiModel.fieldsWithIssues) {
                    IssueItem(it, onClick = onIssueItemClicked)
                }
            }
            Divider(Modifier.padding(horizontal = 24.dp))
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.weight(1f, false)
        ) {
            Button(
                modifier = Modifier.testTag(SECONDARY_BUTTON_TAG),
                shape = if (
                    bottomSheetDialogUiModel.secondaryButton is DialogButtonStyle.NeutralButton
                ) {
                    RoundedCornerShape(24.dp)
                } else {
                    RoundedCornerShape(0.dp)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    disabledBackgroundColor = Color.White
                ),
                elevation = if (
                    bottomSheetDialogUiModel.secondaryButton is DialogButtonStyle.NeutralButton
                ) {
                    ButtonDefaults.elevation(2.dp)
                } else {
                    ButtonDefaults.elevation(0.dp)
                },
                onClick = { onSecondaryButtonClicked() },
                content = provideButtonContent(bottomSheetDialogUiModel.secondaryButton),
                enabled = bottomSheetDialogUiModel.secondaryButton != null
            )
            Button(
                modifier = Modifier.testTag(MAIN_BUTTON_TAG),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = bottomSheetDialogUiModel.mainButton.backgroundColor?.let {
                        colorResource(it)
                    } ?: colorResource(id = R.color.colorPrimary),
                    contentColor = Color.White
                ),
                onClick = { onMainButtonClicked() },
                content = provideButtonContent(bottomSheetDialogUiModel.mainButton),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            )
        }
    }
}

private fun provideButtonContent(
    buttonStyle: DialogButtonStyle?
): @Composable (RowScope.() -> Unit) = {
    buttonStyle?.let { style ->
        style.iconResource?.let { icon ->
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "",
                tint = style.colorResource?.let { colorResource(id = it) } ?: Color.Unspecified,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(
            text = stringResource(id = style.textResource),
            color = style.colorResource?.let { colorResource(id = it) } ?: Color.Unspecified
        )
    }
}

@Composable
fun IssueItem(fieldWithIssue: FieldWithIssue, onClick: () -> Unit) {
    Row(
        Modifier
            .testTag(fieldWithIssue.issueType.name)
            .clickable { onClick.invoke() }
    ) {
        Icon(
            painter = painterResource(
                when (fieldWithIssue.issueType) {
                    IssueType.ERROR,
                    IssueType.ERROR_ON_COMPLETE,
                    IssueType.MANDATORY -> R.drawable.ic_error_outline
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
            Text(
                text = fieldWithIssue.fieldName,
                color = colorResource(id = R.color.textPrimary),
                fontSize = 14.sp
            )
            Text(
                text = fieldWithIssue.message,
                color = colorResource(id = R.color.textSecondary),
                fontSize = 14.sp
            )
        }
    }
}

@Preview
@Composable
fun DialogPreview1() {
    BottomSheetDialogContent(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Saved",
            subtitle = "If you exit now all the information in the form will be discarded.",
            iconResource = R.drawable.ic_saved_check,
            mainButton = DialogButtonStyle.MainButton(R.string.keep_editing),
            secondaryButton = DialogButtonStyle.DiscardButton()
        ),
        onMainButtonClicked = {},
        onSecondaryButtonClicked = {}
    )
}

@Preview
@Composable
fun DialogPreview2() {
    BottomSheetDialogContent(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Saved",
            subtitle = "Do you want to mark this form as complete?",
            iconResource = R.drawable.ic_saved_check,
            mainButton = DialogButtonStyle.CompleteButton(),
            secondaryButton = DialogButtonStyle.SecondaryButton(R.string.not_now)
        ),
        onMainButtonClicked = {},
        onSecondaryButtonClicked = {}
    )
}

@Preview
@Composable
fun DialogPreview3() {
    BottomSheetDialogContent(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Not saved",
            subtitle = "Some mandatory fields are missing and the form cannot be saved.",
            iconResource = R.drawable.ic_alert,
            fieldsWithIssues = listOf(
                FieldWithIssue("uid", "Age", IssueType.MANDATORY, "Field Mandatory")
            ),
            mainButton = DialogButtonStyle.MainButton(R.string.review)
        ),
        onMainButtonClicked = {}
    )
}

@Preview
@Composable
fun DialogPreview4() {
    BottomSheetDialogContent(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Not saved",
            subtitle = "Some fields have errors and they are not saved." +
                "If you exit now the changes will be discarded.",
            iconResource = R.drawable.ic_error_outline,
            fieldsWithIssues = listOf(
                FieldWithIssue("Uid", "Age", IssueType.ERROR, "Enter text"),
                FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text")
            ),
            mainButton = DialogButtonStyle.MainButton(R.string.review),
            secondaryButton = DialogButtonStyle.DiscardButton()
        ),
        onMainButtonClicked = {}
    )
}

const val SECONDARY_BUTTON_TAG = "SECONDARY_BUTTON_TAG"
const val MAIN_BUTTON_TAG = "MAIN_BUTTON_TAG"
