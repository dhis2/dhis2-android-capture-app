package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.dhis2.usescases.teiDashboard.ui.model.TimelineEventsHeaderModel
import org.hisp.dhis.mobile.ui.designsystem.component.Description
import org.hisp.dhis.mobile.ui.designsystem.component.Title
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun TimelineEventsHeader(
    modifier: Modifier = Modifier,
    timelineEventsHeaderModel: TimelineEventsHeaderModel,
    onOptionSelected: (eventCreationType: EventCreationType) -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp),
        horizontalArrangement = spacedBy(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            Title(text = stringResource(id = R.string.timeline))
            Description(
                text =
                    "${timelineEventsHeaderModel.eventCount} ${timelineEventsHeaderModel.eventLabel}",
                textColor = TextColor.OnSurfaceLight,
            )
        }
        if (timelineEventsHeaderModel.displayEventCreationButton) {
            NewEventOptions(
                options = timelineEventsHeaderModel.options,
                addButtonTestTag = TEST_ADD_EVENT_BUTTON_IN_TIMELINE,
                onOptionSelected = onOptionSelected,
            )
        }
    }
}

@Preview(backgroundColor = 0xffffff, showBackground = true)
@Composable
private fun TimelineEventHeaderPreview() {
    TimelineEventsHeader(
        timelineEventsHeaderModel = TimelineEventsHeaderModel(true, 3, "events", listOf()),
        onOptionSelected = {},
    )
}

const val TEST_ADD_EVENT_BUTTON_IN_TIMELINE = "TEST_ADD_EVENT_BUTTON_IN_TIMELINE"
