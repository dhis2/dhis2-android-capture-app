package org.dhis2.uicomponents.map.layer

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.checkbox.MaterialCheckBox
import org.dhis2.R
import org.dhis2.databinding.DialogMapLayerBinding
import org.dhis2.uicomponents.map.layer.types.EnrollmentMapLayer
import org.dhis2.uicomponents.map.layer.types.EventMapLayer
import org.dhis2.uicomponents.map.layer.types.HeatmapMapLayer
import org.dhis2.uicomponents.map.layer.types.RelationshipMapLayer
import org.dhis2.uicomponents.map.layer.types.SatelliteMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiMapLayer
import org.dhis2.utils.ColorUtils

class MapLayerDialog(
    private val mapLayerManager: MapLayerManager
) : DialogFragment() {

    companion object {
        const val MARGIN = 12
        const val IMAGE_SIZE = 40
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    private val layerVisibility: HashMap<String, Boolean> = hashMapOf()
    lateinit var binding: DialogMapLayerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_map_layer, container, false)

        initProgramData()
        initListeners()

        return binding.root
    }

    private fun initProgramData() {
        mapLayerManager.mapLayers.toSortedMap().forEach { (source, layer) ->
            layerVisibility[source] ?: run { layerVisibility[source] = layer.visible }
            when (layer) {
                is TeiMapLayer -> addCheckBox(
                    source,
                    context!!.getString(R.string.dialog_layer_tei_coordinates),
                    MapLayerManager.TEI_ICON_ID
                )
                is EnrollmentMapLayer -> addCheckBox(
                    source,
                    context!!.getString(R.string.dialog_layer_enrollment_coordinates),
                    MapLayerManager.ENROLLMENT_ICON_ID
                )
                is HeatmapMapLayer -> addCheckBox(
                    source,
                    context!!.getString(R.string.dialog_layer_heatmap)
                )
                is SatelliteMapLayer -> addCheckBox(
                    source,
                    context!!.getString(R.string.dialog_layer_satellite)
                )
                is RelationshipMapLayer -> addCheckBox(source)
                is EventMapLayer -> addCheckBox(source)
            }
        }
    }

    private fun initListeners() {
        binding.acceptButton.setOnClickListener {
            layerVisibility.filterKeys { it != LayerType.SATELLITE_LAYER.toString() }.forEach {
                mapLayerManager.handleLayer(it.key, it.value)
            }
            dismiss()
        }
    }

    private fun addCheckBox(source: String, layerText: String? = null, image: String? = null) {
        binding.layout.addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(
                    MaterialCheckBox(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(MARGIN, MARGIN, MARGIN, MARGIN) }
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
                            if (source == LayerType.SATELLITE_LAYER.toString()) {
                                mapLayerManager.handleLayer(source, isChecked)
                            }
                            layerVisibility[source] = isChecked
                        }
                    }
                )
                image?.let {
                    addView(
                        ImageView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                IMAGE_SIZE,
                                IMAGE_SIZE
                            ).apply { marginEnd = MARGIN }
                            setImageBitmap(mapLayerManager.mapboxMap.style?.getImage(image))
                        }
                    )
                }
            }
        )
    }
}
