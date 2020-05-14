package org.dhis2.uicomponents.map.layer

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.DialogMapLayerBinding
import org.hisp.dhis.android.core.D2

class MapLayerDialog(
    private val teiIcon: Bitmap,
    private val enrollmentIcon: Bitmap?,
    val styleCallback: (Boolean) -> Unit
) : DialogFragment() {

    private lateinit var d2: D2
    private lateinit var layerManager: MapLayerManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        d2 = (context.applicationContext as App).serverComponent.userManager().d2
        layerManager = MapLayerManager.instance()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    lateinit var binding: DialogMapLayerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_map_layer, container, false)

        initLiveData()
        initListeners()
        initProgramData()

        return binding.root
    }

    private fun initProgramData() {
        binding.teiIcon.setImageBitmap(teiIcon)
        binding.enrollmentIcon.setImageBitmap(enrollmentIcon)
    }

    private fun initListeners() {
        binding.styleCheck.setOnCheckedChangeListener { _, isChecked ->
            styleCallback(isChecked)
            layerManager.setSatelliteLayer(isChecked)
        }

        binding.teiCheck.setOnCheckedChangeListener { _, isChecked ->
            layerManager.setTeiLayer(isChecked)
        }

        binding.enrollmentCheck.setOnCheckedChangeListener { _, isChecked ->
            layerManager.setEnrollmentLayer(isChecked)
        }
        binding.heatmapCheck.setOnCheckedChangeListener { _, isChecked ->
            layerManager.setHeapMapLayer(isChecked)
        }
        binding.acceptButton.setOnClickListener { dismiss() }
    }

    private fun initLiveData() {
        layerManager.setSatelliteStyle().observe(this,
            Observer {
                binding.styleCheck.isChecked = it
            }
        )

        layerManager.showTeiLayer().observe(
            this,
            Observer {
                binding.teiCheck.isChecked = it
            }
        )

        layerManager.showEnrollmentLayer().observe(
            this,
            Observer {
                binding.enrollmentCheck.isChecked = it
            }
        )

        layerManager.showHeatMapLayer().observe(
            this,
            Observer {
                binding.heatmapCheck.isChecked = it
            }
        )
    }
}
