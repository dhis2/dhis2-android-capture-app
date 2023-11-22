package org.dhis2.usescases.datasets.datasetDetail.datasetList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.card.MaterialCardView
import org.dhis2.R
import org.dhis2.databinding.ItemDatasetBinding
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.datasetList.mapper.DatasetCardMapper
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard

class DataSetListAdapter(
    val viewModel: DataSetListViewModel,
    private val cardMapper: DatasetCardMapper,
) :
    ListAdapter<DataSetDetailModel, DataSetListViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataSetListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDatasetBinding.inflate(inflater, parent, false)
        return DataSetListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataSetListViewHolder, position: Int) {
        getItem(position)?.let {
            val materialCardView =
                holder.itemView.findViewById<MaterialCardView>(R.id.cardView)
            materialCardView.visibility = View.GONE
            val composeView = holder.itemView.findViewById<ComposeView>(R.id.composeView)
            composeView.setContent {
                val card = cardMapper.map(
                    dataset = it,
                    editable = viewModel.isEditable(
                        datasetUid = it.datasetUid(),
                        periodId = it.periodId(),
                        organisationUnitUid = it.orgUnitUid(),
                        attributeOptionComboUid = it.catOptionComboUid(),
                    ),
                    onSyncIconClick = {
                        viewModel.syncDataSet(it)
                    },
                    onCardCLick = {
                        viewModel.openDataSet(it)
                    },
                )
                ListCard(
                    listAvatar = card.avatar,
                    title = card.title,
                    lastUpdated = card.lastUpdated,
                    additionalInfoList = card.additionalInfo,
                    actionButton = card.actionButton,
                    expandLabelText = card.expandLabelText,
                    shrinkLabelText = card.shrinkLabelText,
                    onCardClick = card.onCardCLick,
                )
            }

            holder.bind(it, viewModel)
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<DataSetDetailModel>() {
    override fun areItemsTheSame(
        oldItem: DataSetDetailModel,
        newItem: DataSetDetailModel,
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: DataSetDetailModel,
        newItem: DataSetDetailModel,
    ): Boolean {
        return oldItem == newItem
    }
}
