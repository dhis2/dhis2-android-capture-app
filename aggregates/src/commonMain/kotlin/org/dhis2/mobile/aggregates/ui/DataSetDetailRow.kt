package org.dhis2.mobile.aggregates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.TextAlignment
import org.hisp.dhis.mobile.ui.designsystem.component.AssistChip
import org.hisp.dhis.mobile.ui.designsystem.component.Tag
import org.hisp.dhis.mobile.ui.designsystem.component.TagType
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
internal fun DataSetDetails(
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    dataSetDetails: DataSetDetails,
) {
    val titleDetails = dataSetDetails.customTitle
    val columnContentAlignment = getColumContentAlignment(titleDetails.textAlignment)
    val rowContentAlignment = getRowContentAlignment(titleDetails.textAlignment)
    val textAlignment by remember { derivedStateOf { getTextAlignment(titleDetails.textAlignment) } }

    Box(Modifier.background(MaterialTheme.colorScheme.primary)) {
        Column(
            modifier =
                modifier
                    .clip(
                        RoundedCornerShape(topStart = Radius.L, topEnd = Radius.L),
                    ).background(MaterialTheme.colorScheme.surfaceBright),
            horizontalAlignment = columnContentAlignment,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.Spacing8, horizontal = Spacing.Spacing16),
                horizontalAlignment = columnContentAlignment,
            ) {
                titleDetails.header?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier,
                        textAlign = textAlignment,
                        color = TextColor.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                titleDetails.subHeader?.let { subHeaderText ->
                    if (titleDetails.header == null) Spacer(Modifier.height(Spacing.Spacing16))
                    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
                    val textLayoutResult = textLayoutResultState.value
                    val subHeaderTextStyle =
                        MaterialTheme.typography.bodyMedium
                            .copy(color = TextColor.OnSurfaceLight)
                            .toSpanStyle()
                    var annotatedText by remember {
                        mutableStateOf(
                            buildAnnotatedString {
                                withStyle(style = ParagraphStyle(lineHeight = 20.sp)) {
                                    withStyle(style = subHeaderTextStyle) {
                                        append(subHeaderText)
                                    }
                                }
                            },
                        )
                    }
                    if (textLayoutResult?.hasVisualOverflow == true) {
                        val lastCharIndex = textLayoutResult.getLineEnd(1)
                        val adjustedText =
                            subHeaderText
                                .substring(startIndex = 0, endIndex = lastCharIndex)
                                .dropLast(3)
                                .dropLastWhile { it == ' ' || it == '.' }

                        val textWithAppliedOverflow = "$adjustedText..."

                        annotatedText =
                            buildAnnotatedString {
                                withStyle(style = subHeaderTextStyle) {
                                    append(textWithAppliedOverflow)
                                }
                            }
                    }
                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        color = TextColor.OnSurfaceLight,
                        textAlign = textAlignment,
                        onTextLayout = { textLayoutResultState.value = it },
                        maxLines = 2,
                    )
                    Spacer(modifier = Modifier.height(Spacing.Spacing8))
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = Spacing.Spacing16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = rowContentAlignment,
            ) {
                item(key = dataSetDetails.dateLabel) {
                    if (editable) {
                        AssistChip(
                            label = dataSetDetails.dateLabel,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "",
                                )
                            },
                            onClick = {
                                // not yet supported
                            },
                        )
                    } else {
                        Tag(
                            label = dataSetDetails.dateLabel,
                            type = TagType.DEFAULT,
                        )
                    }
                    Spacer(Modifier.size(Spacing.Spacing8))
                }

                item(key = dataSetDetails.orgUnitLabel) {
                    if (editable) {
                        AssistChip(
                            label = dataSetDetails.orgUnitLabel,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.AccountTree,
                                    contentDescription = "",
                                )
                            },
                            onClick = {
                                // not yet supported
                            },
                        )
                    } else {
                        Tag(
                            label = dataSetDetails.orgUnitLabel,
                            type = TagType.DEFAULT,
                        )
                    }
                    Spacer(Modifier.size(Spacing.Spacing8))
                }

                dataSetDetails.catOptionComboLabel?.let { catOptionComboLabel ->
                    item(key = catOptionComboLabel) {
                        if (editable) {
                            AssistChip(
                                label = catOptionComboLabel,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Category,
                                        contentDescription = "",
                                    )
                                },
                                onClick = {
                                    // not yet supported
                                },
                            )
                        } else {
                            Tag(
                                label = catOptionComboLabel,
                                type = TagType.DEFAULT,
                            )
                        }
                        Spacer(Modifier.size(Spacing.Spacing8))
                    }
                }
            }
        }
    }
}

private fun getTextAlignment(textAlignment: TextAlignment?) =
    when (textAlignment) {
        TextAlignment.LEFT -> TextAlign.Start
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.RIGHT -> TextAlign.End
        else -> TextAlign.Center
    }

private fun getColumContentAlignment(textAlignment: TextAlignment?) =
    when (textAlignment) {
        TextAlignment.LEFT -> Alignment.Start
        TextAlignment.CENTER -> Alignment.CenterHorizontally
        TextAlignment.RIGHT -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

private fun getRowContentAlignment(textAlignment: TextAlignment?) =
    when (textAlignment) {
        TextAlignment.LEFT -> Arrangement.Start
        TextAlignment.CENTER -> Arrangement.Center
        TextAlignment.RIGHT -> Arrangement.End
        else -> Arrangement.Center
    }
