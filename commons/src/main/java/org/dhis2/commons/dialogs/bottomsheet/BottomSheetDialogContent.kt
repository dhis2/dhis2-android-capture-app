package org.dhis2.commons.dialogs.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun BottomSheetDialogUi(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    onMainButtonClicked: () -> Unit,
    onSecondaryButtonClicked: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    icon: @Composable (() -> Unit)? = null,
    extraContent: @Composable
    (() -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier
                .background(color = Color.White, shape = RoundedCornerShape(28.dp, 28.dp))
                .padding(24.dp)
                .fillMaxWidth(),
        verticalArrangement = spacedBy(24.dp),
    ) {
        Column(
            horizontalAlignment = CenterHorizontally,
            modifier =
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
        ) {
            icon?.invoke() ?: Icon(
                painter = painterResource(bottomSheetDialogUiModel.iconResource),
                contentDescription = "",
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = bottomSheetDialogUiModel.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .testTag(TITLE)
                        .padding(horizontal = 16.dp),
            )
            bottomSheetDialogUiModel.subtitle?.let { subtitle ->
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextColor.OnDisabledSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            bottomSheetDialogUiModel.message?.let { message ->
                Spacer(modifier = Modifier.size(16.dp))
                if (bottomSheetDialogUiModel.clickableWord == null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextColor.OnSurfaceLight,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    ClickableText(
                        modifier =
                            Modifier
                                .testTag(CLICKABLE_TEXT_TAG)
                                .fillMaxWidth(),
                        text =
                            buildAnnotatedString {
                                val originalText = message
                                val clickableWord = bottomSheetDialogUiModel.clickableWord!!
                                val clickableWordIndex = originalText.indexOf(clickableWord)
                                append(originalText)
                                addStyle(
                                    style =
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline,
                                        ),
                                    start = clickableWordIndex,
                                    end = clickableWordIndex + clickableWord.length,
                                )
                            },
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = TextColor.OnSurfaceLight,
                                textAlign = TextAlign.Start,
                            ),
                        onClick = {
                            onMessageClick()
                        },
                    )
                }
            }
        }
        extraContent?.let {
            HorizontalDivider()
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
            ) {
                it.invoke()
            }
        }

        if (bottomSheetDialogUiModel.hasButtons()) {
            HorizontalDivider()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                (
                    bottomSheetDialogUiModel.secondaryButton?.textLabel
                        ?: bottomSheetDialogUiModel.secondaryButton?.textResource?.let {
                            stringResource(
                                id = it,
                            )
                        }
                )?.let {
                    Button(
                        modifier = Modifier.testTag(SECONDARY_BUTTON_TAG),
                        shape =
                            RoundedCornerShape(
                                bottomSheetDialogUiModel.secondaryRoundedCornersSizeDp().dp,
                            ),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                disabledContainerColor = Color.White,
                            ),
                        elevation =
                            ButtonDefaults.buttonElevation(
                                bottomSheetDialogUiModel.secondaryElevationDp().dp,
                            ),
                        onClick = { onSecondaryButtonClicked() },
                        content = provideButtonContent(bottomSheetDialogUiModel.secondaryButton),
                        enabled = bottomSheetDialogUiModel.secondaryButton != null,
                    )
                }

                (
                    bottomSheetDialogUiModel.mainButton?.textLabel
                        ?: bottomSheetDialogUiModel.mainButton?.textResource?.let {
                            stringResource(
                                id = it,
                            )
                        }
                )?.let {
                    Button(
                        modifier = Modifier.testTag(MAIN_BUTTON_TAG),
                        style = ButtonStyle.FILLED,
                        onClick = { onMainButtonClicked() },
                        text = it,
                    )
                }
            }
        }
    }
}

const val ERROR_MESSAGE = "Enter text"
const val DATE_BIRTH = "Date of birth"

private fun provideButtonContent(buttonStyle: DialogButtonStyle?): @Composable (RowScope.() -> Unit) =
    {
        buttonStyle?.let { style ->
            style.iconResource?.let { icon ->
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "",
                    tint = style.colorResource ?: Color.Unspecified,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Text(
                text =
                    style.textLabel ?: stringResource(id = style.textResource)
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                color = style.colorResource ?: Color.Unspecified,
            )
        }
    }

@Composable
fun IssueItem(
    fieldWithIssue: FieldWithIssue,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .testTag(fieldWithIssue.issueType.name)
            .clickable { onClick.invoke() },
    ) {
        Icon(
            painter =
                painterResource(
                    when (fieldWithIssue.issueType) {
                        IssueType.ERROR,
                        IssueType.ERROR_ON_COMPLETE,
                        IssueType.MANDATORY,
                        -> R.drawable.ic_error_outline

                        else -> R.drawable.ic_warning_alert
                    },
                ),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier =
                Modifier
                    .width(20.dp)
                    .height(20.dp)
                    .align(Alignment.CenterVertically),
        )
        Column(Modifier.padding(start = 11.dp)) {
            Text(
                text = fieldWithIssue.fieldName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
            )
            if (fieldWithIssue.message.isNotEmpty()) {
                Text(
                    text = fieldWithIssue.message,
                    color = TextColor.OnSurfaceLight,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Preview
@Composable
fun DialogPreview1() {
    BottomSheetDialogUi(
        bottomSheetDialogUiModel =
            BottomSheetDialogUiModel(
                title = "Saved",
                message = "If you exit now all the information in the form will be discarded.",
                iconResource = R.drawable.ic_saved_check,
                mainButton = DialogButtonStyle.MainButton(R.string.keep_editing),
                secondaryButton = DialogButtonStyle.DiscardButton(),
            ),
        onMainButtonClicked = {},
        onSecondaryButtonClicked = {},
    )
}

@Preview
@Composable
fun DialogPreview2() {
    BottomSheetDialogUi(
        bottomSheetDialogUiModel =
            BottomSheetDialogUiModel(
                title = "Saved",
                message = "Do you want to mark this form as complete?",
                iconResource = R.drawable.ic_saved_check,
                mainButton = DialogButtonStyle.CompleteButton,
                secondaryButton = DialogButtonStyle.SecondaryButton(R.string.not_now),
            ),
        onMainButtonClicked = {},
        onSecondaryButtonClicked = {},
    )
}

@Preview
@Composable
fun DialogPreview3() {
    val fieldsWithIssues =
        listOf(
            FieldWithIssue("uid", "Age", IssueType.MANDATORY, "Field Mandatory"),
        )
    BottomSheetDialogUi(
        bottomSheetDialogUiModel =
            BottomSheetDialogUiModel(
                title = "Not saved",
                message = "Some mandatory fields are missing and the form cannot be saved.",
                iconResource = R.drawable.ic_warning_alert,
                mainButton = DialogButtonStyle.MainButton(R.string.review),
            ),
        onMainButtonClicked = {},
    ) {
        ErrorFieldList(fieldsWithIssues = fieldsWithIssues)
    }
}

@Preview
@Composable
fun DialogPreview4() {
    val fieldsWithIssues =
        listOf(
            FieldWithIssue("Uid", "Age", IssueType.ERROR, ERROR_MESSAGE),
            FieldWithIssue("Uid", DATE_BIRTH, IssueType.ERROR, ""),
            FieldWithIssue("Uid", DATE_BIRTH, IssueType.ERROR, ERROR_MESSAGE),
            FieldWithIssue("Uid", DATE_BIRTH, IssueType.ERROR, ERROR_MESSAGE),
            FieldWithIssue("Uid", DATE_BIRTH, IssueType.ERROR, ERROR_MESSAGE),
            FieldWithIssue("Uid", DATE_BIRTH, IssueType.ERROR, ERROR_MESSAGE),
            FieldWithIssue("Uid", DATE_BIRTH, IssueType.ERROR, ERROR_MESSAGE),
            FieldWithIssue("Uid", DATE_BIRTH, IssueType.ERROR, ERROR_MESSAGE),
        )
    BottomSheetDialogUi(
        bottomSheetDialogUiModel =
            BottomSheetDialogUiModel(
                title = "Not saved",
                message =
                    "Some fields have errors and they are not saved." +
                        "If you exit now the changes will be discarded.",
                iconResource = R.drawable.ic_error_outline,
                mainButton = DialogButtonStyle.MainButton(R.string.review),
                secondaryButton = DialogButtonStyle.DiscardButton(),
            ),
        onMainButtonClicked = {},
    ) {
        ErrorFieldList(fieldsWithIssues = fieldsWithIssues)
    }
}

@Preview
@Composable
fun SubtitleNoMessageNoContentDialogPreview() {
    BottomSheetDialogUi(
        bottomSheetDialogUiModel =
            BottomSheetDialogUiModel(
                title = "Title",
                subtitle = "subtitle",
                iconResource = R.drawable.ic_warning_alert,
                mainButton = DialogButtonStyle.MainButton(R.string.review),
            ),
        onMainButtonClicked = {},
    )
}

const val CLICKABLE_TEXT_TAG = "CLICKABLE_TEXT_TAG"
const val SECONDARY_BUTTON_TAG = "SECONDARY_BUTTON_TAG"
const val MAIN_BUTTON_TAG = "MAIN_BUTTON_TAG"
const val TITLE = "TITLE_TAG"
