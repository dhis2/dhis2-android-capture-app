package org.dhis2.ui.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

val colorGray = Color(0xFFF0F6FA)
val textColor = Color(0xFF212121)
val dividerColor = Color(0xFFEAEAEA)

@Composable
fun FormSection(
    modifier: Modifier = Modifier,
    sectionNumber: Int,
    sectionLabel: String,
    fieldCount: Int,
    completedFieldCount: Int,
    errorCount: Int,
    warningCount: Int,
    collapsableState: CollapsableState,
    onSectionClick: () -> Unit,
) {
    val sectionTopPadding = if (collapsableState == CollapsableState.FIXED) {
        32.dp
    } else {
        14.dp
    }

    val sectionBottomPadding = if (collapsableState == CollapsableState.FIXED) {
        8.dp
    } else {
        0.dp
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(enabled = collapsableState != CollapsableState.FIXED) { onSectionClick() }
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = sectionTopPadding, bottom = sectionBottomPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(16.dp),
        ) {
            SectionNumber(sectionNumber = sectionNumber)
            SectionName(
                modifier = Modifier.weight(1f),
                sectionName = sectionLabel,
            )
            when {
                errorCount > 0 || warningCount > 0 ->
                    ErrorWarningCounters(errorCount = errorCount, warningCount = warningCount)

                else ->
                    SectionCompletedFields(
                        completedFieldsText = "$completedFieldCount/$fieldCount",
                        areAllFieldsCompleted = fieldCount == completedFieldCount,
                    )
            }

            if (collapsableState != CollapsableState.FIXED) {
                Icon(
                    modifier = Modifier,
                    imageVector = if (collapsableState == CollapsableState.OPENED) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                )
            }
        }
        if (collapsableState == CollapsableState.CLOSED) {
            Divider(
                modifier = Modifier.padding(top = 14.dp),
                thickness = 1.dp,
                color = dividerColor,
            )
        }
    }
}

@Composable
internal fun SectionNumber(sectionNumber: Int) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(color = colorGray, shape = Shape.Full),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sectionNumber.toString(),
            textAlign = TextAlign.Center,
            style = TextStyle.Default.copy(fontSize = 9.sp, color = textColor),
        )
    }
}

@Composable
internal fun SectionName(modifier: Modifier = Modifier, sectionName: String) {
    Text(
        modifier = modifier,
        text = sectionName,
        style = TextStyle.Default.copy(fontSize = 16.sp, color = textColor),
    )
}

@Composable
internal fun SectionCompletedFields(
    modifier: Modifier = Modifier,
    completedFieldsText: String,
    areAllFieldsCompleted: Boolean,
) {
    Text(
        modifier = modifier,
        text = completedFieldsText,
        style = TextStyle.Default.copy(
            fontSize = 12.sp,
            color = textColor,
        ),
    )
}

@Composable
internal fun ErrorWarningCounters(errorCount: Int, warningCount: Int) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
    ) {
        if (errorCount > 0) ErrorCounter(errorCount = errorCount)
        if (warningCount > 0) WarningCounter(warningCount = warningCount)
    }
}

@Composable
internal fun ErrorCounter(errorCount: Int) {
    Text(
        modifier = Modifier
            .background(
                color = SurfaceColor.ErrorContainer,
                shape = Shape.Small,
            )
            .padding(horizontal = 4.dp),
        text = pluralStringResource(
            id = R.plurals.error_count,
            count = errorCount,
            errorCount,
        ),
        style = TextStyle.Default.copy(
            fontSize = 14.sp,
            color = TextColor.OnErrorContainer,
        ),
    )
}

@Composable
internal fun WarningCounter(warningCount: Int) {
    Text(
        modifier = Modifier
            .background(
                color = SurfaceColor.WarningContainer,
                shape = Shape.Small,
            )
            .padding(horizontal = 4.dp),
        text = pluralStringResource(
            id = R.plurals.warning_count,
            count = warningCount,
            warningCount,
        ),
        style = TextStyle.Default.copy(
            fontSize = 14.sp,
            color = TextColor.OnWarningContainer,
        ),
    )
}

@Preview
@Composable
fun SectionPreview() {
    Column(modifier = Modifier) {
        FormSection(
            sectionNumber = 1,
            sectionLabel = "Section name",
            fieldCount = 8,
            completedFieldCount = 3,
            errorCount = 0,
            warningCount = 0,
            collapsableState = CollapsableState.CLOSED,
        ) {
        }
        FormSection(
            sectionNumber = 1,
            sectionLabel = "This name that is very long and overflow because it is too long",
            fieldCount = 8,
            completedFieldCount = 3,
            errorCount = 0,
            warningCount = 0,
            collapsableState = CollapsableState.OPENED,
        ) {
        }
        FormSection(
            sectionNumber = 1,
            sectionLabel = "Section name",
            fieldCount = 8,
            completedFieldCount = 3,
            errorCount = 0,
            warningCount = 0,
            collapsableState = CollapsableState.FIXED,
        ) {
        }
        FormSection(
            sectionNumber = 1,
            sectionLabel = "Section name",
            fieldCount = 8,
            completedFieldCount = 3,
            errorCount = 1,
            warningCount = 0,
            collapsableState = CollapsableState.FIXED,
        ) {
        }
        FormSection(
            sectionNumber = 1,
            sectionLabel = "Section name",
            fieldCount = 8,
            completedFieldCount = 3,
            errorCount = 1,
            warningCount = 2,
            collapsableState = CollapsableState.FIXED,
        ) {
        }
    }
}
