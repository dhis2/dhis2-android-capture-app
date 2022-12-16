package org.dhis2.maps.layer

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.R
import org.dhis2.maps.databinding.DialogMapLayerBinding
import org.dhis2.maps.databinding.ItemLayerBinding
import org.dhis2.maps.layer.basemaps.BasemapAdapter
import org.dhis2.maps.layer.types.EnrollmentMapLayer
import org.dhis2.maps.layer.types.EventMapLayer
import org.dhis2.maps.layer.types.FieldMapLayer
import org.dhis2.maps.layer.types.HEATMAP_ICON
import org.dhis2.maps.layer.types.HeatmapMapLayer
import org.dhis2.maps.layer.types.RelationshipMapLayer
import org.dhis2.maps.layer.types.TeiEventMapLayer
import org.dhis2.maps.layer.types.TeiMapLayer
import org.dhis2.maps.managers.EventMapManager
import org.dhis2.maps.managers.MapManager
import org.dhis2.maps.managers.RelationshipMapManager.Companion.RELATIONSHIP_ICON

class MapLayerDialog(
    private val mapManager: MapManager
) : BottomSheetDialogFragment() {

    private val layerVisibility: HashMap<String, Boolean> = hashMapOf()
    lateinit var binding: DialogMapLayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_map_layer, container, false)
        binding.baseMapCarousel.adapter = BasemapAdapter(mapManager.mapLayerManager)
        binding.acceptButton.setTextColor(
            ColorStateList.valueOf(
                ColorUtils.getPrimaryColor(
                    context,
                    ColorUtils.ColorType.PRIMARY
                )
            )
        )
        initProgramData()
        initListeners()

        return binding.root
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.setPeekHeight(0)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet =
                (it as BottomSheetDialog).findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) as FrameLayout?
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
        return dialog
    }

    private fun initProgramData() {
        val layerMap: LinkedHashMap<String, MutableList<View>> = linkedMapOf(
            Pair("TEI", mutableListOf()),
            Pair("ENROLLMENT", mutableListOf()),
            Pair("TRACKER_EVENT", mutableListOf()),
            Pair("RELATIONSHIP", mutableListOf()),
            Pair("EVENT", mutableListOf()),
            Pair("DE", mutableListOf()),
            Pair("HEATMAP", mutableListOf())
        )
        mapManager.mapLayerManager.mapLayers.toSortedMap().forEach { (source, layer) ->
            layerVisibility[source] ?: run { layerVisibility[source] = layer.visible }
            when (layer) {
                is TeiMapLayer -> layerMap["TEI"]?.add(
                    addCheckBox(
                        source,
                        requireContext().getString(R.string.dialog_layer_tei_coordinates),
                        MapLayerManager.TEI_ICON_ID
                    )
                )
                is EnrollmentMapLayer -> layerMap["ENROLLMENT"]?.add(
                    addCheckBox(
                        source,
                        requireContext().getString(R.string.dialog_layer_enrollment_coordinates),
                        MapLayerManager.ENROLLMENT_ICON_ID
                    )
                )
                is TeiEventMapLayer -> layerMap["TRACKER_EVENT"]?.add(
                    addCheckBox(
                        source,
                        image = "${MapLayerManager.STAGE_ICON_ID}_$source"
                    )
                )
                is HeatmapMapLayer -> layerMap["HEATMAP"]?.add(
                    addCheckBox(
                        source,
                        requireContext().getString(R.string.dialog_layer_heatmap),
                        HEATMAP_ICON
                    )
                )
                is RelationshipMapLayer -> layerMap["RELATIONSHIP"]?.add(
                    addCheckBox(
                        source,
                        null,
                        "${RELATIONSHIP_ICON}_$source"
                    )
                )
                is EventMapLayer -> layerMap["EVENT"]?.add(
                    addCheckBox(
                        source,
                        requireContext().getString(R.string.dialog_layer_event),
                        EventMapManager.ICON_ID
                    )
                )
                is FieldMapLayer -> layerMap["DE"]?.add(
                    addCheckBox(
                        source,
                        mapManager.getLayerName(source),
                        "${EventMapManager.DE_ICON_ID}_$source"
                    )
                )
            }
        }
        layerMap.forEach {
            if (it.value.isNotEmpty()) {
                it.value.forEach { checkBox ->
                    binding.layout.addView(checkBox)
                }
            }
        }
    }

    private fun initListeners() {
        binding.acceptButton.setOnClickListener {
            layerVisibility.forEach { (sourceId, visible) ->
                mapManager.mapLayerManager.handleLayer(sourceId, visible)
            }
            mapManager.carouselAdapter?.updateLayers(mapManager.mapLayerManager.mapLayers)
            dismiss()
        }
    }

    private fun addCheckBox(
        source: String,
        layerText: String? = null,
        image: String? = null
    ): View {
        return ItemLayerBinding.inflate(LayoutInflater.from(context)).apply {
            root.tag = "tag_$source"
            layerCheckBox.apply {
                text = layerText ?: source
                isChecked = layerVisibility[source] ?: false
                CompoundButtonCompat.setButtonTintList(
                    this,
                    ColorStateList.valueOf(
                        ColorUtils.getPrimaryColor(
                            context,
                            ColorUtils.ColorType.PRIMARY
                        )
                    )
                )
                setOnCheckedChangeListener { _, isChecked ->
                    layerVisibility[source] = isChecked
                }
            }
            image?.let {
                if (it == HEATMAP_ICON) {
                    layerIcon.setImageResource(R.drawable.ic_heatmap_icon)
                } else {
                    layerIcon.setImageBitmap(
                        mapManager.mapLayerManager.mapboxMap.style?.getImage(
                            image
                        )
                    )
                }
            }
        }.root
    }
}
