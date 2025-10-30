package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.data.StageSection
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

class ToggleStageEventsButtonHolder(
    val composeView: ComposeView,
    private val stageSelector: FlowableProcessor<StageSection>,
) : RecyclerView.ViewHolder(composeView) {
    fun bind(eventModel: EventModel) {
        composeView.setContent {
            Button(
                modifier =
                    Modifier.padding(
                        start =
                            if (eventModel.groupedByStage == true) {
                                Spacing.Spacing48
                            } else {
                                Spacing.Spacing0
                            },
                    ),
                style = ButtonStyle.TEXT,
                text =
                    if (eventModel.showAllEvents) {
                        composeView.context.getString(R.string.show_less_events)
                    } else {
                        composeView.context.getString(
                            R.string.show_more_events,
                            (eventModel.eventCount - eventModel.maxEventsToShow).toString(),
                        )
                    },
            ) {
                stageSelector.onNext(
                    StageSection(
                        stageUid = eventModel.stage?.uid() ?: "",
                        showOptions = false,
                        showAllEvents = !eventModel.showAllEvents,
                    ),
                )
            }
        }
    }
}
