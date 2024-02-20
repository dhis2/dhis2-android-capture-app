package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle

class ToggleStageEventsButtonHolder(
    val composeView: ComposeView,
    private val stageSelector: FlowableProcessor<StageSection>,
) : RecyclerView.ViewHolder(composeView) {
    fun bind(
        eventViewModel: EventViewModel,
    ) {
        composeView.setContent {
            Button(
                style = ButtonStyle.TEXT,
                text = if (eventViewModel.showAllEvents) {
                    composeView.context.getString(R.string.show_less_events)
                } else {
                    composeView.context.getString(
                        R.string.show_more_events,
                        (eventViewModel.eventCount - eventViewModel.maxEventsToShow).toString(),
                    )
                },
            ) {
                stageSelector.onNext(
                    StageSection(
                        stageUid = eventViewModel.stage?.uid() ?: "",
                        showOptions = false,
                        showAllEvents = !eventViewModel.showAllEvents,
                    ),
                )
            }
        }
    }
}
