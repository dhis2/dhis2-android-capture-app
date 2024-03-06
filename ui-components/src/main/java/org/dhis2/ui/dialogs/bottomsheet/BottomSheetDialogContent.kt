package org.dhis2.ui.dialogs.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import org.dhis2.ui.ErrorFieldList
import org.dhis2.ui.R
import org.dhis2.ui.icons.SyncingIcon
import org.dhis2.ui.items.SyncStatusItem
import org.dhis2.ui.theme.colorPrimary
import org.dhis2.ui.theme.textPrimary
import org.dhis2.ui.theme.textSecondary
import org.dhis2.ui.theme.textSubtitle

@Composable
fun BottomSheetDialogUi(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    onMainButtonClicked: () -> Unit,
    onSecondaryButtonClicked: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    icon: @Composable (() -> Unit)? = null,
    extraContent: @Composable
    (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .background(color = Color.White, shape = RoundedCornerShape(28.dp, 28.dp))
            .padding(24.dp)
            .fillMaxWidth(),
        verticalArrangement = spacedBy(24.dp)
    ) {
        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            icon?.invoke() ?: Icon(
                painter = painterResource(bottomSheetDialogUiModel.iconResource),
                contentDescription = "",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = bottomSheetDialogUiModel.title,
                style = MaterialTheme.typography.headlineSmall,
                color = textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag(TITLE)
                    .padding(horizontal = 16.dp)
            )
            bottomSheetDialogUiModel.subtitle?.let { subtitle ->
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSubtitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            bottomSheetDialogUiModel.message?.let { message ->
                Spacer(modifier = Modifier.size(16.dp))
                if (bottomSheetDialogUiModel.clickableWord == null) {
                    Text(
                        text = message,
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
                            val originalText = message
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
        }
        extraContent?.let {
            Divider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
            ) {
                it.invoke()
            }
        }

        if (bottomSheetDialogUiModel.hasButtons()) {
            Divider()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.testTag(SECONDARY_BUTTON_TAG),
                    shape = RoundedCornerShape(
                        bottomSheetDialogUiModel.secondaryRoundedCornersSizeDp().dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        bottomSheetDialogUiModel.secondaryElevationDp().dp
                    ),
                    onClick = { onSecondaryButtonClicked() },
                    content = provideButtonContent(bottomSheetDialogUiModel.secondaryButton),
                    enabled = bottomSheetDialogUiModel.secondaryButton != null
                )
                Button(
                    modifier = Modifier.testTag(MAIN_BUTTON_TAG),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bottomSheetDialogUiModel.mainButton?.backgroundColor
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
            text = style.textLabel ?: stringResource(id = style.textResource)
                .capitalize(Locale.current),
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
                    else -> R.drawable.ic_warning_alert
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
    BottomSheetDialogUi(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Saved",
            message = "If you exit now all the information in the form will be discarded.",
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
    BottomSheetDialogUi(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Saved",
            message = "Do you want to mark this form as complete?",
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
    val fieldsWithIssues = listOf(
        FieldWithIssue("uid", "Age", IssueType.MANDATORY, "Field Mandatory")
    )
    BottomSheetDialogUi(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Not saved",
            message = "Some mandatory fields are missing and the form cannot be saved.",
            iconResource = R.drawable.ic_warning_alert,
            mainButton = DialogButtonStyle.MainButton(R.string.review)
        ),
        onMainButtonClicked = {}
    ) {
        ErrorFieldList(fieldsWithIssues = fieldsWithIssues)
    }
}

@Preview
@Composable
fun DialogPreview4() {
    val fieldsWithIssues = listOf(
        FieldWithIssue("Uid", "Age", IssueType.ERROR, "Enter text"),
        FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text"),
        FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text"),
        FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text"),
        FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text"),
        FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text"),
        FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text"),
        FieldWithIssue("Uid", "Date of birth", IssueType.ERROR, "Enter text")
    )
    BottomSheetDialogUi(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Not saved",
            message = "Some fields have errors and they are not saved." +
                "If you exit now the changes will be discarded.",
            iconResource = R.drawable.ic_error_outline,
            mainButton = DialogButtonStyle.MainButton(R.string.review),
            secondaryButton = DialogButtonStyle.DiscardButton()
        ),
        onMainButtonClicked = {}
    ) {
        ErrorFieldList(fieldsWithIssues = fieldsWithIssues)
    }
}

@Preview
@Composable
fun SubtitleDialogPreview() {
    BottomSheetDialogUi(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Title",
            subtitle = "subtitle",
            message = "Content message. Content message. Content message",
            iconResource = R.drawable.ic_warning_alert,
            mainButton = DialogButtonStyle.MainButton(R.string.review)
        ),
        onMainButtonClicked = {}
    ) {
        LazyColumn(
            verticalArrangement = spacedBy(8.dp)
        ) {
            items(listOf("a")) {
                SyncStatusItem(
                    title = "Name",
                    subtitle = "Description",
                    onClick = {
                    }
                ) {
                    SyncingIcon()
                }
            }
        }
    }
}

@Preview
@Composable
fun SubtitleNoMessageDialogPreview() {
    BottomSheetDialogUi(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Title",
            subtitle = "subtitle",
            iconResource = R.drawable.ic_warning_alert,
            mainButton = DialogButtonStyle.MainButton(R.string.review)
        ),
        onMainButtonClicked = {}
    ) {
        LazyColumn(
            verticalArrangement = spacedBy(8.dp)
        ) {
            items(listOf("a")) {
                SyncStatusItem(
                    title = "Name",
                    subtitle = "Description",
                    onClick = {
                    }
                ) {
                    SyncingIcon()
                }
            }
        }
    }
}

@Preview
@Composable
fun SubtitleNoMessageNoContentDialogPreview() {
    BottomSheetDialogUi(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = "Title",
            subtitle = "subtitle",
            iconResource = R.drawable.ic_warning_alert,
            mainButton = DialogButtonStyle.MainButton(R.string.review)
        ),
        onMainButtonClicked = {}
    )
}

const val CLICKABLE_TEXT_TAG = "CLICKABLE_TEXT_TAG"
const val SECONDARY_BUTTON_TAG = "SECONDARY_BUTTON_TAG"
const val MAIN_BUTTON_TAG = "MAIN_BUTTON_TAG"
const val TITLE = "TITLE_TAG"
