package org.dhis2.usescases.map.polygon

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PolygonAdapter(
        val list: List<PolygonViewModel.PolygonPoint>,
        val viewModel: PolygonViewModel
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_EMPTY = 1
    private val VIEW_TYPE_FULL = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemViewType(position: Int): Int {
        if (position == list.size) {
            return VIEW_TYPE_EMPTY
        }
        return VIEW_TYPE_FULL
    }

    inner class Holder(): RecyclerView.ViewHolder() {

    }
}