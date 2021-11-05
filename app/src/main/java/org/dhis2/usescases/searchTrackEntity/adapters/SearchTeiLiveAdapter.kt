package org.dhis2.usescases.searchTrackEntity.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.databinding.ItemSearchErrorBinding
import org.dhis2.databinding.ItemSearchTrackedEntityBinding
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule
import org.dhis2.utils.customviews.ImageDetailBottomDialog

class SearchTeiLiveAdapter(
    private val fromRelationship: Boolean,
    private val presenter: SearchTEContractsModule.Presenter,
    private val fm: FragmentManager
) : PagedListAdapter<SearchTeiModel, RecyclerView.ViewHolder>(SearchAdapterDiffCallback()) {

    private enum class SearchItem {
        TEI,
        RELATIONSHIP_TEI,
        ONLINE_ERROR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (SearchItem.values()[viewType]) {
            SearchItem.TEI -> SearchTEViewHolder(
                ItemSearchTrackedEntityBinding.inflate(inflater, parent, false)
            )
            SearchItem.RELATIONSHIP_TEI -> SearchRelationshipViewHolder(
                ItemSearchTrackedEntityBinding.inflate(inflater, parent, false)
            )
            SearchItem.ONLINE_ERROR -> SearchErrorViewHolder(
                ItemSearchErrorBinding.inflate(inflater, parent, false)
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
                    presenter,
                    getItem(position)!!,
                    {
                        getItem(holder.absoluteAdapterPosition)?.toggleAttributeList()
                        notifyItemChanged(holder.absoluteAdapterPosition)
                    }
                ) { path: String? ->
                    path?.let {
                        ImageDetailBottomDialog(null, File(path))
                            .show(fm, ImageDetailBottomDialog.TAG)
                    }
                }
            is SearchErrorViewHolder -> holder.bind(getItem(position)!!)
        }
    }

    fun clearList() {
        submitList(null)
    }
}
