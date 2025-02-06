package org.dhis2.mobile.aggregates.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.hisp.dhis.mobile.ui.designsystem.component.AssistChip
import org.hisp.dhis.mobile.ui.designsystem.component.Tag
import org.hisp.dhis.mobile.ui.designsystem.component.TagType
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
internal fun DataSetDetails(
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    dataSetDetails: DataSetDetails,
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(Spacing.Spacing8),
    ) {
        item {
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
                        /*not yet supported*/
                    },
                )
            } else {
                Tag(
                    label = dataSetDetails.dateLabel,
                    type = TagType.DEFAULT,
                )
            }
        }

        item {
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
                        /*not yet supported*/
                    },
                )
            } else {
                Tag(
                    label = dataSetDetails.orgUnitLabel,
                    type = TagType.DEFAULT,
                )
            }
        }

        dataSetDetails.catOptionComboLabel?.let { catOptionComboLabel ->
            item {
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
                            /*not yet supported*/
                        },
                    )
                } else {
                    Tag(
                        label = catOptionComboLabel,
                        type = TagType.DEFAULT,
                    )
                }
            }
        }
    }
}
