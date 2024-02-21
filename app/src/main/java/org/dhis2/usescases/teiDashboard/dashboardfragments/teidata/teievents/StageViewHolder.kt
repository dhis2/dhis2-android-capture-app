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
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.MetadataIcon
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teiDashboard.ui.NewEventOptions

internal class StageViewHolder(
    val composeView: ComposeView,
    private val stageSelector: FlowableProcessor<StageSection>,
    private val presenter: TEIDataPresenter,
    private val colorUtils: ColorUtils,
) : RecyclerView.ViewHolder(composeView) {

    fun bind(eventItem: EventViewModel) {
        val stage = eventItem.stage!!

        val color = colorUtils.getColorFrom(
            stage.style().color(),
            colorUtils.getPrimaryColor(
                itemView.context,
                ColorType.PRIMARY_LIGHT,
            ),
        )

        val resourceManager = ResourceManager(itemView.context, colorUtils)

        val iconResource = resourceManager
            .getObjectStyleDrawableResource(
                stage.style().icon(),
                R.drawable.ic_default_outline,
            )

        composeView.setContent {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetadataIcon(
                    metadataIconData = MetadataIconData(
                        programColor = color,
                        iconResource = iconResource,
                        sizeInDp = 40,
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stage.displayName() ?: "",
                        color = colorResource(id = R.color.textPrimary),
                        fontSize = 14.sp,
                        style = LocalTextStyle.current.copy(
                            fontFamily = FontFamily(Font(R.font.rubik_regular)),
                        ),
                    )
                    if (eventItem.eventCount < 1) {
                        Text(
                            text = stringResource(R.string.no_data),
                            color = colorResource(id = R.color.textSecondary),
                            fontSize = 12.sp,
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.rubik_regular)),
                            ),
                        )
                    }
                }
                if (eventItem.canShowAddButton()) {
                    Box(modifier = Modifier
                        .clickable {
                            stageSelector.onNext(
                                StageSection(
                                    stageUid = stage.uid(),
                                    showOptions = true,
                                    showAllEvents = false,
                                ),
                            )
                        }) {
                        NewEventOptions(presenter.getNewEventOptionsByStages(stage)) {
                            presenter.onAddNewEventOptionSelected(it, stage)
                        }
                    }
                }
            }
        }
    }
}
