package org.dhis2.usescases.datasets.datasetDetail.datasetList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.semantics
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.card.MaterialCardView
import org.dhis2.R
import org.dhis2.commons.ui.ListCardProvider
import org.dhis2.databinding.ItemDatasetBinding
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.datasetList.mapper.DatasetCardMapper
import org.dhis2.utils.adapterItemPosition
import org.dhis2.utils.adapterItemTitle
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

class DataSetListAdapter(
    val viewModel: DataSetListViewModel,
    private val cardMapper: DatasetCardMapper,
) : ListAdapter<DataSetDetailModel, DataSetListViewHolder>(ItemDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DataSetListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDatasetBinding.inflate(inflater, parent, false)
        return DataSetListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DataSetListViewHolder,
        position: Int,
    ) {
        getItem(position)?.let {
            val materialCardView =
                holder.itemView.findViewById<MaterialCardView>(R.id.cardView)
            materialCardView.visibility = View.GONE
            val composeView = holder.itemView.findViewById<ComposeView>(R.id.composeView)
            composeView.setContent {
                val card =
                    cardMapper.map(
                        dataset = it,
                        editable =
                            viewModel.isEditable(
                                datasetUid = it.datasetUid,
                                periodId = it.periodId,
                                organisationUnitUid = it.orgUnitUid,
                                attributeOptionComboUid = it.catOptionComboUid,
                            ),
                        onSyncIconClick = {
                            viewModel.syncDataSet(it)
                        },
                        onCardCLick = {
                            viewModel.openDataSet(it)
                        },
                    )
                Column(
                    modifier =
                        Modifier
                            .padding(
                                start = Spacing.Spacing8,
                                end = Spacing.Spacing8,
                                bottom = Spacing.Spacing4,
                            ),
                ) {
                    if (position == 0) {
                        Spacer(modifier = Modifier.size(Spacing.Spacing8))
                    }
                    ListCardProvider(
                        modifier =
                            Modifier.semantics {
                                adapterItemPosition = position
                                adapterItemTitle = card.title
                            },
                        card = card,
                        syncingResourceId = R.string.syncing,
                    )
                }
            }

            holder.bind(it, viewModel)
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<DataSetDetailModel>() {
    override fun areItemsTheSame(
        oldItem: DataSetDetailModel,
        newItem: DataSetDetailModel,
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
        oldItem: DataSetDetailModel,
        newItem: DataSetDetailModel,
    ): Boolean = oldItem == newItem
}
