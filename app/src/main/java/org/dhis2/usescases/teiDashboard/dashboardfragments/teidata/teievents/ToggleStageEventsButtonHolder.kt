package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection

class ToggleStageEventsButtonHolder(
    val composeView: ComposeView,
    private val stageSelector: FlowableProcessor<StageSection>,
) : RecyclerView.ViewHolder(composeView) {
    fun bind(
        eventViewModel: EventViewModel
    ) {

        composeView.setContent {
            MdcTheme {
                Text(
                    modifier = Modifier
                        .clickable {
                            stageSelector.onNext(
                                StageSection(
                                    stageUid = eventViewModel.stage?.uid() ?: "",
                                    showOptions = false,
                                    showAllEvents = !eventViewModel.showAllEvents
                                )
                            )
                        },
                    text = if (eventViewModel.showAllEvents)
                        composeView.context.getString(R.string.show_less_events)
                    else
                        composeView.context.getString(R.string.show_more_events, (eventViewModel.eventCount - 3).toString())
                )
            }
        }
    }
}