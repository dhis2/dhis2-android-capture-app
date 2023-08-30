package org.dhis2.usescases.searchTrackEntity.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemSearchErrorBinding
import org.dhis2.databinding.ItemSearchTrackedEntityBinding

class SearchTeiLiveAdapter(
    private val fromRelationship: Boolean,
    private val colorUtils: ColorUtils,
    private val onAddRelationship: (
        teiUid: String,
        relationshipTypeUid: String?,
        isOnline: Boolean,
    ) -> Unit,
    private val onSyncIconClick: (teiUid: String) -> Unit,
    private val onDownloadTei: (teiUid: String, enrollmentUid: String?) -> Unit,
    private val onTeiClick: (teiUid: String, enrollmentUid: String?, isOnline: Boolean) -> Unit,
    private val onImageClick: (imagePath: String) -> Unit,
) : PagedListAdapter<SearchTeiModel, RecyclerView.ViewHolder>(SearchAdapterDiffCallback()) {

    private enum class SearchItem {
        TEI,
        RELATIONSHIP_TEI,
        ONLINE_ERROR,
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (SearchItem.values()[viewType]) {
            SearchItem.TEI -> SearchTEViewHolder(
                ItemSearchTrackedEntityBinding.inflate(inflater, parent, false),
                onSyncIconClick,
                onDownloadTei,
                colorUtils,
                onTeiClick,
            )
            SearchItem.RELATIONSHIP_TEI -> SearchRelationshipViewHolder(
                ItemSearchTrackedEntityBinding.inflate(inflater, parent, false),
                colorUtils,
                onAddRelationship,
            )
            SearchItem.ONLINE_ERROR -> SearchErrorViewHolder(
                ItemSearchErrorBinding.inflate(inflater, parent, false),
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            getItem(position)?.onlineErrorMessage != null -> SearchItem.ONLINE_ERROR.ordinal
            fromRelationship -> SearchItem.RELATIONSHIP_TEI.ordinal
            else -> SearchItem.TEI.ordinal
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BaseTeiViewHolder ->
                holder.bind(
                    getItem(position)!!,
                    {
                        getItem(holder.absoluteAdapterPosition)?.toggleAttributeList()
                        notifyItemChanged(holder.absoluteAdapterPosition)
                    },
                ) { path: String? ->
                    path?.let { onImageClick(path) }
                }
            is SearchErrorViewHolder -> holder.bind(getItem(position)!!)
        }
    }

    fun clearList() {
        submitList(null)
    }
}
