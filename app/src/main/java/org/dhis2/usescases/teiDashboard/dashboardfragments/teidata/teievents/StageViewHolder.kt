package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.MetadataIcon
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teiDashboard.ui.NewEventOptions
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyle
import org.hisp.dhis.mobile.ui.designsystem.component.Description
import org.hisp.dhis.mobile.ui.designsystem.component.Title
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

internal class StageViewHolder(
    val composeView: ComposeView,
    private val stageSelector: FlowableProcessor<StageSection>,
    private val presenter: TEIDataPresenter,
    private val colorUtils: ColorUtils,
) : RecyclerView.ViewHolder(composeView) {

    fun bind(eventItem: EventViewModel) {
        val stage = eventItem.stage!!

        val resourceManager = ResourceManager(itemView.context, colorUtils)

        composeView.setContent {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProvideAvatar(
                    metadataIconData = eventItem.metadataIconData,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f),
                ) {
                    Title(text = stage.displayName() ?: "")
                    if (eventItem.eventCount < 1) {
                        Description(
                            text = resourceManager.getString(R.string.no_data),
                            textColor = TextColor.OnSurfaceLight,
                        )
                    }
                }
                if (eventItem.canShowAddButton()) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                stageSelector.onNext(
                                    StageSection(
                                        stageUid = stage.uid(),
                                        showOptions = true,
                                        showAllEvents = false,
                                    ),
                                )
                            },
                    ) {
                        NewEventOptions(presenter.getNewEventOptionsByStages(stage)) {
                            presenter.onAddNewEventOptionSelected(it, stage)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ProvideAvatar(
        metadataIconData: MetadataIconData,
    ) {
        Avatar(
            metadataAvatar = {
                MetadataIcon(metadataIconData = metadataIconData)
            },
            style = AvatarStyle.METADATA,
        )
    }
}
