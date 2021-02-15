package org.dhis2.uicomponents.map.carousel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Feature
import org.dhis2.databinding.ItemCarouselEventBinding
import org.dhis2.databinding.ItemCarouselProgramEventBinding
import org.dhis2.databinding.ItemCarouselRelationshipBinding
import org.dhis2.databinding.ItemCarouselTeiBinding
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.layer.MapLayer
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.dhis2.uicomponents.map.layer.types.RelationshipMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiEventMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiMapLayer
import org.dhis2.uicomponents.map.model.CarouselItemModel
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.dhis2.usescases.programEventDetail.ProgramEventViewModel
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.program.Program

class CarouselAdapter private constructor(
    private val currentTei: String,
    private val program: Program?,
    private val onDeleteRelationshipListener: (relationshipUid: String) -> Boolean,
    private val onSyncClickListener: (String) -> Boolean,
    private val onTeiClickListener: (String, String?, Boolean) -> Boolean,
    private val onRelationshipClickListener: (relationshipTeiUid: String) -> Boolean,
    private val onEventClickListener: (
        uid: String?,
        enrollmentUid: String?,
        eventUid: String?
    ) -> Boolean,
    private val onProfileImageClick: (String) -> Unit,
    private val allItems: MutableList<CarouselItemModel>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items: MutableList<CarouselItemModel> = arrayListOf()

    enum class CarouselItems {
        TEI, RELATIONSHIP, EVENT, PROGRAM_EVENT
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
                    onSyncClickListener,
                    onProfileImageClick,
                    { item ->
                        (items.first { it == item } as SearchTeiModel).toggleAttributeList()
                        notifyItemChanged(items.indexOf(item))
                    }
                )
            CarouselItems.RELATIONSHIP ->
                CarouselRelationshipHolder(
                    ItemCarouselRelationshipBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    currentTei,
                    onDeleteRelationshipListener,
                    onRelationshipClickListener
                )
            CarouselItems.EVENT ->
                CarouselEventHolder(
                    ItemCarouselEventBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    program,
                    onEventClickListener,
                    onProfileImageClick
                )
            CarouselItems.PROGRAM_EVENT ->
                CarouselProgramEventHolder(
                    ItemCarouselProgramEventBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onEventClickListener,
                    { item ->
                        (items.first { it == item } as ProgramEventViewModel).toggleAttributeList()
                    }
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CarouselTeiHolder -> holder.bind(items[position] as SearchTeiModel)
            is CarouselRelationshipHolder ->
                holder.bind(items[position] as RelationshipUiComponentModel)
            is CarouselEventHolder -> holder.bind(items[position] as EventUiComponentModel)
            is CarouselProgramEventHolder -> holder.bind(items[position] as ProgramEventViewModel)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchTeiModel -> CarouselItems.TEI.ordinal
            is RelationshipUiComponentModel -> CarouselItems.RELATIONSHIP.ordinal
            is EventUiComponentModel -> CarouselItems.EVENT.ordinal
            is ProgramEventViewModel -> CarouselItems.PROGRAM_EVENT.ordinal
            else -> -1
        }
    }

    fun update(sourceId: String, mapLayer: MapLayer?, visible: Boolean) {
        when (mapLayer) {
            is TeiMapLayer ->
                updateItems(allItems.filterIsInstance<SearchTeiModel>(), visible)
            is RelationshipMapLayer ->
                updateItems(
                    allItems.filterIsInstance<RelationshipUiComponentModel>()
                        .filter { it.displayName == sourceId },
                    visible
                )
            is TeiEventMapLayer ->
                updateItems(
                    allItems.filterIsInstance<EventUiComponentModel>()
                        .filter { it.programStage?.displayName() == sourceId },
                    visible
                )
            else -> Unit
        }
    }

    private fun updateItems(data: List<CarouselItemModel>, visible: Boolean) {
        when (visible) {
            true ->
                data.filter { !items.contains(it) }.takeIf { it.isNotEmpty() }
                    ?.let { addItems(it) }
            false -> removeItems(data)
        }
    }

    fun addItems(data: List<CarouselItemModel>) {
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun updateAllData(data: List<CarouselItemModel>, mapLayerManager: MapLayerManager) {
        allItems.clear()
        allItems.addAll(data)
        items.clear()
        mapLayerManager.mapLayers.forEach { (sourceId, mapLayer) ->
            update(sourceId, mapLayer, mapLayer.visible)
        }
    }

    fun removeItems(data: List<CarouselItemModel>) {
        items.removeAll(data)
        notifyDataSetChanged()
    }

    fun updateItem(carouselItem: CarouselItemModel) {
        allItems.takeIf { it.isNotEmpty() }?.indexOfFirst { it.uid() == carouselItem.uid() }?.let {
            allItems[it] = carouselItem
        }
        items.indexOfFirst { it.uid() == carouselItem.uid() }.let {
            items[it] = carouselItem
            notifyItemChanged(it)
        }
    }

    fun indexOfFeature(feature: Feature): Int {
        val item = items.firstOrNull {
            when (it) {
                is SearchTeiModel ->
                    it.tei.uid() == feature.getStringProperty(
                        MapTeisToFeatureCollection.TEI_UID
                    )
                is RelationshipUiComponentModel ->
                    it.relationshipUid == feature.getStringProperty(
                        MapRelationshipsToFeatureCollection.RELATIONSHIP_UID
                    )
                is EventUiComponentModel ->
                    it.eventUid == feature.getStringProperty(
                        MapTeiEventsToFeatureCollection.EVENT_UID
                    )
                is ProgramEventViewModel ->
                    it.uid() == feature.getStringProperty(
                        MapEventToFeatureCollection.EVENT
                    )
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
                is RelationshipUiComponentModel -> it.relationshipUid
                is EventUiComponentModel -> it.eventUid
                is ProgramEventViewModel -> it.uid()
                else -> ""
            }
        }
    }

    inline fun <reified T : Any> getType(): Class<T> {
        return T::class.java
    }

    data class Builder(
        var currentTei: String = "",
        var onDeleteRelationshipListener: (relationshipUid: String) -> Boolean = { false },
        var onSyncClickListener: (String) -> Boolean = { true },
        var onTeiClickListener: (String, String?, Boolean) -> Boolean =
            { _: String, _: String?, _: Boolean -> true },
        var onRelationshipClickListener: (relationshipTeiUid: String) -> Boolean = { false },
        var onEventClickListener: (String?, String?, String?) -> Boolean =
            { _: String?, _: String?, _: String? -> false },
        var onProfileImageClick: (String) -> Unit = { },
        var items: MutableList<CarouselItemModel> = arrayListOf(),
        var program: Program? = null
    ) {
        fun addCurrentTei(currentTei: String) = apply {
            this.currentTei = currentTei
        }

        fun addOnTeiClickListener(
            onTeiClick: (
                teiUid: String,
                enrollmentUid: String?,
                isOnline: Boolean
            ) -> Boolean
        ) = apply {
            this.onTeiClickListener = onTeiClick
        }

        fun addOnDeleteRelationshipListener(
            onDeleteRelationship: (relationshipUid: String) -> Boolean
        ) = apply {
            this.onDeleteRelationshipListener = onDeleteRelationship
        }

        fun addOnRelationshipClickListener(
            onRelationshipClickListener: (relationshipTeiUid: String) -> Boolean
        ) = apply {
            this.onRelationshipClickListener = onRelationshipClickListener
        }

        fun addOnSyncClickListener(onSyncClick: (String) -> Boolean) = apply {
            this.onSyncClickListener = onSyncClick
        }

        fun addOnEventClickListener(
            onEventClickListener: (
                uid: String?,
                enrollmentUid: String?,
                eventUid: String?
            ) -> Boolean
        ) = apply {
            this.onEventClickListener = onEventClickListener
        }

        fun addOnProfileImageClickListener(
            onProfileImageClick: (String) -> Unit
        ) = apply {
            this.onProfileImageClick = onProfileImageClick
        }

        fun addProgram(program: Program?) = apply {
            this.program = program
        }

        fun addItems(items: MutableList<CarouselItemModel>) = apply {
            this.items = items
        }

        fun build() = CarouselAdapter(
            currentTei,
            program,
            onDeleteRelationshipListener,
            onSyncClickListener,
            onTeiClickListener,
            onRelationshipClickListener,
            onEventClickListener,
            onProfileImageClick,
            items
        )
    }
}
