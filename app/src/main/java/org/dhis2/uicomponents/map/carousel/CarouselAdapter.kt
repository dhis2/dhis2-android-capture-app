package org.dhis2.uicomponents.map.carousel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Feature
import org.dhis2.databinding.ItemCarouselTeiBinding
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel

class CarouselAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CarouselItems {
        TEI, RELATIONSHIP, EVENT
    }

    private var onSyncClickListener: (String) -> Boolean = { true }
    val items: MutableList<T> = mutableListOf()

    private var onTeiClickListener: (String, String?, Boolean) -> Boolean =
        { _: String, _: String?, _: Boolean -> true }

    fun addOnTeiClickListener(
        onTeiClick: (
            teiUid: String,
            enrollmentUid: String?,
            isOnline: Boolean
        ) -> Boolean
    ) {
        this.onTeiClickListener = onTeiClick
    }

    fun addOnSyncClickListener(onSyncClick: (String) -> Boolean) {
        this.onSyncClickListener = onSyncClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (CarouselItems.values()[viewType]) {
            CarouselItems.TEI ->
                CarouselTeiHolder(
                    ItemCarouselTeiBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onTeiClickListener,
                    onSyncClickListener
                )
            else -> throw IllegalArgumentException("View type not supported")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CarouselTeiHolder -> holder.bind(items[position] as SearchTeiModel)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchTeiModel -> CarouselItems.TEI.ordinal
            else -> -1
        }
    }

    fun addItems(data: List<T>) {
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun indexOfFeature(feature: Feature): Int {
        val item = items.firstOrNull {
            when (it) {
                is SearchTeiModel -> it.tei.uid() == feature.getStringProperty("teiUid")
                else -> false
            }
        }

        return item?.let {
            items.indexOf(item)
        } ?: 0
    }

    fun getUidProperty(selectedPosition: Int): String {
        return items[selectedPosition].let {
            when (it) {
                is SearchTeiModel -> it.tei.uid()
                else -> ""
            }
        }
    }
}
