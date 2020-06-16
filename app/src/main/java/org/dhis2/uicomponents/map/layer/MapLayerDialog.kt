package org.dhis2.uicomponents.map.layer

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import org.dhis2.R
import org.dhis2.databinding.DialogMapLayerBinding
import org.dhis2.uicomponents.map.managers.TeiMapManager

class MapLayerDialog(
    private val teiMapManager: TeiMapManager
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    private var teiValue: Boolean? = null
    private var enrollmentValue: Boolean? = null
    private var heatmapValue: Boolean? = null
    private var satelliteValue: Boolean? = null
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
        binding.teiIcon.setImageBitmap(
            teiMapManager.style?.getImage(MapLayerManager.TEI_ICON_ID)
        )
        binding.enrollmentIcon.setImageBitmap(
            teiMapManager.style?.getImage(MapLayerManager.ENROLLMENT_ICON_ID)
        )
        binding.teiCheck.isChecked = true
    }

    private fun initListeners() {
        binding.styleCheck.setOnCheckedChangeListener { _, isChecked ->
            if (satelliteValue != isChecked) {
                teiMapManager.mapLayerManager.handleLayer(LayerType.SATELLITE_LAYER, isChecked)
                satelliteValue = isChecked
            }
        }
        binding.teiCheck.setOnCheckedChangeListener { _, isChecked ->
            teiValue = isChecked
        }
        binding.enrollmentCheck.setOnCheckedChangeListener { _, isChecked ->
            enrollmentValue = isChecked
        }
        binding.heatmapCheck.setOnCheckedChangeListener { _, isChecked ->
            heatmapValue = isChecked
        }
        binding.acceptButton.setOnClickListener {
            teiValue?.let {
                teiMapManager.mapLayerManager.handleLayer(TeiMapManager.TEIS_SOURCE_ID, it)
                teiValue = null
            }
            enrollmentValue?.let {
                teiMapManager.mapLayerManager.handleLayer(TeiMapManager.ENROLLMENT_SOURCE_ID, it)
                enrollmentValue = null
            }
            heatmapValue?.let {
                teiMapManager.mapLayerManager.handleLayer(LayerType.HEATMAP_LAYER, it)
                heatmapValue = null
            }
            dismiss()
        }
    }
}
