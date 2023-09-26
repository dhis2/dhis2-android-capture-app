package org.dhis2.maps.carousel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Feature
import org.dhis2.commons.data.CarouselItemModel
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.databinding.ItemCarouselEventBinding
import org.dhis2.maps.databinding.ItemCarouselProgramEventBinding
import org.dhis2.maps.databinding.ItemCarouselRelationshipBinding
import org.dhis2.maps.databinding.ItemCarouselTeiBinding
import org.dhis2.maps.extensions.FeatureSource
import org.dhis2.maps.extensions.source
import org.dhis2.maps.geometry.TEI_UID
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection.Companion.EVENT_UID
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.types.EnrollmentMapLayer
import org.dhis2.maps.layer.types.EventMapLayer
import org.dhis2.maps.layer.types.FieldMapLayer
import org.dhis2.maps.layer.types.RelationshipMapLayer
import org.dhis2.maps.layer.types.TeiEventMapLayer
import org.dhis2.maps.layer.types.TeiMapLayer
import org.dhis2.maps.managers.MapManager
import org.dhis2.maps.model.EventUiComponentModel
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.hisp.dhis.android.core.program.Program

class CarouselAdapter private constructor(
    private val currentTei: String,
    private val program: Program?,
    private val onDeleteRelationshipListener: (relationshipUid: String) -> Boolean,
    private val onSyncClickListener: (String) -> Boolean,
    private val onTeiClickListener: (String, String?, Boolean) -> Boolean,
    private val onRelationshipClickListener: (
        relationshipTeiUid: String,
        ownerType: RelationshipOwnerType,
    ) -> Boolean,
    private val onEventClickListener: (
        uid: String?,
        enrollmentUid: String?,
        eventUid: String?,
    ) -> Boolean,
    private val onProfileImageClick: (String) -> Unit,
    private val onNavigateListener: (String) -> Unit,
    private val allItems: MutableList<CarouselItemModel>,
    private val mapManager: MapManager?,
    private val colorUtils: ColorUtils,
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
                        false,
                    ),
                    colorUtils,
                    onTeiClickListener,
                    onSyncClickListener,
                    onNavigateListener,
                    onProfileImageClick,
                    { item ->
                        (items.first { it == item } as SearchTeiModel).toggleAttributeList()
                        notifyItemChanged(items.indexOf(item))
                    },
                )
            CarouselItems.RELATIONSHIP ->
                CarouselRelationshipHolder(
                    ItemCarouselRelationshipBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    ),
                    currentTei,
                    onDeleteRelationshipListener,
                    onRelationshipClickListener,
                    onNavigateListener,
                )
            CarouselItems.EVENT ->
                CarouselEventHolder(
                    ItemCarouselEventBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    ),
                    program,
                    onEventClickListener,
                    onProfileImageClick,
                    onNavigateListener,
                )
            CarouselItems.PROGRAM_EVENT ->
                CarouselProgramEventHolder(
                    ItemCarouselProgramEventBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    ),
                    onEventClickListener,
                    { item ->
                        (items.first { it == item } as ProgramEventViewModel).toggleAttributeList()
                    },
                    onNavigateListener,
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

    fun updateLayers(mapLayers: HashMap<String, MapLayer>?) {
        val teisToShow = mutableListOf<CarouselItemModel>()
        val relationshipsToShow = mutableListOf<CarouselItemModel>()
        val teiEventToShow = mutableListOf<CarouselItemModel>()
        val eventToShow = mutableListOf<CarouselItemModel>()
        mapLayers?.forEach { (sourceId, currentLayer) ->
            when (currentLayer) {
                is TeiMapLayer -> {
                    if (currentLayer.visible) {
                        teisToShow.addAll(
                            allItems.filterIsInstance<SearchTeiModel>().filter {
                                it.tei.geometry() != null
                            },
                        )
                    }
                }
                is EnrollmentMapLayer -> {
                    if (currentLayer.visible) {
                        teisToShow.addAll(
                            allItems.filterIsInstance<SearchTeiModel>().filter {
                                it.selectedEnrollment?.geometry() != null
                            },
                        )
                    }
                }
                is FieldMapLayer -> {
                    if (currentLayer.visible) {
                        teisToShow.addAll(
                            allItems.filterIsInstance<SearchTeiModel>().filter {
                                mapManager?.findFeature(sourceId, TEI_UID, it.uid()) != null
                            },
                        )
                        eventToShow.addAll(
                            allItems.filterIsInstance<ProgramEventViewModel>().filter {
                                mapManager?.findFeature(sourceId, EVENT_UID, it.uid()) != null
                            },
                        )
                    }
                }
                is RelationshipMapLayer -> {
                    if (currentLayer.visible) {
                        relationshipsToShow.addAll(
                            allItems.filterIsInstance<RelationshipUiComponentModel>()
                                .filter { it.displayName == sourceId },
                        )
                    }
                }
                is TeiEventMapLayer -> {
                    if (currentLayer.visible) {
                        teiEventToShow.addAll(
                            allItems.filterIsInstance<EventUiComponentModel>()
                                .filter { it.programStage?.displayName() == sourceId },
                        )
                    }
                }
                is EventMapLayer -> {
                    if (currentLayer.visible) {
                        eventToShow.addAll(
                            allItems.filterIsInstance<ProgramEventViewModel>(),
                        )
                    }
                }
            }
        }

        this.items.apply {
            clear()
            addAll(teisToShow.distinct())
            addAll(relationshipsToShow)
            addAll(teiEventToShow)
            addAll(eventToShow)
        }

        notifyDataSetChanged()
    }

    fun addItems(data: List<CarouselItemModel>) {
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun setAllItems(data: List<CarouselItemModel>) {
        allItems.clear()
        allItems.addAll(data)
    }

    fun setItems(data: List<CarouselItemModel>, searchInitial: Boolean = false) {
        allItems.clear()
        items.clear()
        allItems.addAll(data)
        if (searchInitial) {
            items.addAll(data.filterIsInstance<SearchTeiModel>())
        } else {
            items.addAll(data)
        }
        notifyDataSetChanged()
    }

    private fun removeItems(data: List<CarouselItemModel>) {
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
        val item = when (feature.source()) {
            FeatureSource.TEI, FeatureSource.ENROLLMENT ->
                items.filterIsInstance<SearchTeiModel>()
                    .firstOrNull {
                        it.tei.uid() == feature.getStringProperty(
                            MapTeisToFeatureCollection.TEI_UID,
                        )
                    }
            FeatureSource.RELATIONSHIP ->
                items.filterIsInstance<RelationshipUiComponentModel>()
                    .firstOrNull {
                        it.relationshipUid == feature.getStringProperty(
                            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
                        )
                    }
            FeatureSource.TRACKER_EVENT ->
                items.filterIsInstance<EventUiComponentModel>()
                    .firstOrNull {
                        it.eventUid == feature.getStringProperty(
                            MapTeiEventsToFeatureCollection.EVENT_UID,
                        )
                    }
            FeatureSource.EVENT ->
                items.filterIsInstance<ProgramEventViewModel>()
                    .firstOrNull {
                        it.uid() == feature.getStringProperty(
                            MapEventToFeatureCollection.EVENT,
                        )
                    }
            FeatureSource.FIELD ->
                items.firstOrNull {
                    it.uid() == feature.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
                        ?: feature.getStringProperty(MapTeiEventsToFeatureCollection.EVENT_UID)
                }

            null -> null
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
        var onRelationshipClickListener: (
            relationshipTeiUid: String,
            ownerType: RelationshipOwnerType,
        ) -> Boolean = { _: String?, _: RelationshipOwnerType -> false },
        var onEventClickListener: (String?, String?, String?) -> Boolean =
            { _: String?, _: String?, _: String? -> false },
        var onProfileImageClick: (String) -> Unit = { },
        var onNavigateClickListener: (String) -> Unit = { },
        var items: MutableList<CarouselItemModel> = arrayListOf(),
        var program: Program? = null,
        var mapManager: MapManager? = null,
        val colorUtils: ColorUtils = ColorUtils(),
    ) {
        fun addCurrentTei(currentTei: String?) = apply {
            if (currentTei != null) {
                this.currentTei = currentTei
            }
        }

        fun addOnTeiClickListener(
            onTeiClick: (
                teiUid: String,
                enrollmentUid: String?,
                isOnline: Boolean,
            ) -> Boolean,
        ) = apply {
            this.onTeiClickListener = onTeiClick
        }

        fun addOnDeleteRelationshipListener(
            onDeleteRelationship: (relationshipUid: String) -> Boolean,
        ) = apply {
            this.onDeleteRelationshipListener = onDeleteRelationship
        }

        fun addOnRelationshipClickListener(
            onRelationshipClickListener: (
                relationshipTeiUid: String,
                ownerType: RelationshipOwnerType,
            ) -> Boolean,
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
                eventUid: String?,
            ) -> Boolean,
        ) = apply {
            this.onEventClickListener = onEventClickListener
        }

        fun addOnProfileImageClickListener(onProfileImageClick: (String) -> Unit) = apply {
            this.onProfileImageClick = onProfileImageClick
        }

        fun addProgram(program: Program?) = apply {
            this.program = program
        }

        fun addItems(items: MutableList<CarouselItemModel>) = apply {
            this.items = items
        }

        fun addOnNavigateClickListener(onNavigateClick: (String) -> Unit) = apply {
            this.onNavigateClickListener = onNavigateClick
        }

        fun addMapManager(mapManager: MapManager) = apply {
            this.mapManager = mapManager
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
            onNavigateClickListener,
            items,
            mapManager,
            colorUtils,
        )
    }
}
