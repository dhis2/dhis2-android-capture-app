package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.processors.FlowableProcessor
import org.dhis2.Bindings.toDateSpan
import org.dhis2.R
import org.dhis2.databinding.ItemStageSectionBinding
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataContracts
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.resources.ResourceManager

internal class StageViewHolder(
    private val binding: ItemStageSectionBinding,
    private val stageSelector: FlowableProcessor<String>,
    private val presenter: TEIDataContracts.Presenter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(eventItem: EventViewModel) {
        val stage = eventItem.stage!!

        binding.programStageName.text = stage.displayName()

        val color = ColorUtils.getColorFrom(
            stage.style().color(),
            ColorUtils.getPrimaryColor(
                itemView.context,
                ColorUtils.ColorType.PRIMARY_LIGHT
            )
        )

        binding.programStageIcon.setBackgroundColor(color)
        binding.programStageIcon.setImageResource(
            ResourceManager(itemView.context).getObjectStyleDrawableResource(
                stage.style().icon(),
                R.drawable.ic_program_default
            )
        )
        binding.lastUpdatedEvent.text = eventItem.lastUpdate.toDateSpan(itemView.context)
        binding.addStageButton.visibility = if (stage.repeatable() == true || eventItem.eventCount == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.addStageButton.setOnClickListener {view->
            presenter.onAddNewEvent(view,stage)
        }
        binding.programStageCount.text = "${eventItem.eventCount} Events"

        itemView.setOnClickListener { stageSelector.onNext(stage.uid()) }

    }
}