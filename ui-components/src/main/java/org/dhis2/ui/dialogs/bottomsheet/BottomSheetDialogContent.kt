package org.dhis2.ui.dialogs.bottomsheet

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.R
import org.dhis2.ui.theme.colorPrimary
import org.dhis2.ui.theme.textPrimary
import org.dhis2.ui.theme.textSecondary

@Composable
fun BottomSheetDialogContent(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    onMainButtonClicked: () -> Unit,
    onSecondaryButtonClicked: () -> Unit = {},
    onIssueItemClicked: () -> Unit = {},
    onMessageClick: () -> Unit = {}
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
            modifier = modifier.wrapContentHeight()
        ) {
            Icon(
                painter = painterResource(bottomSheetDialogUiModel.iconResource),
                contentDescription = "",
                tint = Color.Unspecified
            )
            Text(
                text = bottomSheetDialogUiModel.title,
                style = MaterialTheme.typography.headlineSmall,
                color = textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            if (bottomSheetDialogUiModel.clickableWord == null) {
                Text(
                    text = bottomSheetDialogUiModel.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ClickableText(
                    modifier = Modifier
                        .testTag(CLICKABLE_TEXT_TAG)
                        .fillMaxWidth(),
                    text = buildAnnotatedString {
                        val originalText = bottomSheetDialogUiModel.subtitle
                        val clickableWord = bottomSheetDialogUiModel.clickableWord!!
                        val clickableWordIndex = originalText.indexOf(clickableWord)
                        append(originalText)
                        addStyle(
                            style = SpanStyle(
                                color = colorPrimary,
                                textDecoration = TextDecoration.Underline
                            ),
                            start = clickableWordIndex,
                            end = clickableWordIndex + clickableWord.length
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textSecondary,
                        textAlign = TextAlign.Start
                    ),
                    onClick = {
                        onMessageClick()
                    }
                )
            }
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
                    containerColor = Color.White,
                    disabledContainerColor = Color.White
                ),
                elevation = if (
                    bottomSheetDialogUiModel.secondaryButton is DialogButtonStyle.NeutralButton
                ) {
                    ButtonDefaults.buttonElevation(2.dp)
                } else {
                    ButtonDefaults.buttonElevation(0.dp)
                },
                onClick = { onSecondaryButtonClicked() },
                content = provideButtonContent(bottomSheetDialogUiModel.secondaryButton),
                enabled = bottomSheetDialogUiModel.secondaryButton != null
            )
            Button(
                modifier = Modifier.testTag(MAIN_BUTTON_TAG),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = bottomSheetDialogUiModel.mainButton.backgroundColor
                        ?: colorPrimary,
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
                tint = style.colorResource ?: Color.Unspecified,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(
            text = stringResource(id = style.textResource).capitalize(Locale.current),
            color = style.colorResource ?: Color.Unspecified
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
                color = textPrimary,
                fontSize = 14.sp
            )
            Text(
                text = fieldWithIssue.message,
                color = textSecondary,
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

const val CLICKABLE_TEXT_TAG = "CLICKABLE_TEXT_TAG"
const val SECONDARY_BUTTON_TAG = "SECONDARY_BUTTON_TAG"
const val MAIN_BUTTON_TAG = "MAIN_BUTTON_TAG"
