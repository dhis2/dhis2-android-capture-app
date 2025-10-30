package org.dhis2.maps.layer

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.databinding.ObservableInt
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.maps.R
import org.dhis2.maps.databinding.BasemapItemBinding
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
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2TextStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.getTextStyle

class MapLayerDialog : BottomSheetDialogFragment() {
    var mapManager: MapManager? = null
    private var programUid: String? = null
    private var onLayersVisibility: (layersVisibility: HashMap<String, MapLayer>) -> Unit = {}

    private val layerVisibility: HashMap<String, Boolean> = hashMapOf()
    lateinit var resourceManager: ResourceManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        resourceManager = ResourceManager(context, ColorUtils())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        programUid = arguments?.getString(ARG_PROGRAM_UID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = SurfaceColor.SurfaceBright,
                                shape =
                                    RoundedCornerShape(
                                        topStart = Radius.XL,
                                        topEnd = Radius.XL,
                                    ),
                            ).padding(horizontal = Spacing.Spacing24, vertical = Spacing.Spacing16),
                ) {
                    Text(
                        text = stringResource(id = R.string.map_layers),
                        style = getTextStyle(DHIS2TextStyle.LABEL_MEDIUM),
                    )
                    var currentStyle =
                        ObservableInt(mapManager?.mapLayerManager?.currentStylePosition ?: 0)
                    LazyRow {
                        itemsIndexed(
                            mapManager?.mapLayerManager?.baseMapManager?.getBaseMaps()
                                ?: emptyList(),
                        ) { index, baseMap ->
                            AndroidView(
                                modifier =
                                    Modifier
                                        .width(100.dp)
                                        .clickable {
                                            mapManager?.mapLayerManager?.changeStyle(index)
                                            currentStyle.set(index)
                                        },
                                factory = { context ->
                                    BasemapItemBinding
                                        .inflate(
                                            LayoutInflater.from(context),
                                        ).also { binding ->
                                            binding.apply {
                                                currentSelectedStyle = currentStyle
                                                itemStyle = index
                                                if (baseMap.basemapImage != null) {
                                                    baseMapImage.setImageDrawable(baseMap.basemapImage)
                                                    baseMapImage.scaleType =
                                                        ImageView.ScaleType.CENTER_CROP
                                                } else {
                                                    baseMapImage.setBackgroundColor(android.graphics.Color.GRAY)
                                                    baseMapImage.setImageResource(R.drawable.unknown_base_map)
                                                    baseMapImage.scaleType =
                                                        ImageView.ScaleType.FIT_CENTER
                                                }
                                                basemapName.text = baseMap.basemapName
                                            }
                                        }.root
                                },
                                update = {},
                            )
                        }
                    }
                    val layersData = remember { getMapLayer() }
                    MapLayerList(layersData, layerVisibility)
                    Button(
                        text = stringResource(R.string.action_apply),
                        style = ButtonStyle.TEXT,
                    ) {
                        layerVisibility.forEach { (sourceId, visible) ->
                            mapManager?.mapLayerManager?.handleLayer(sourceId, visible)
                        }
                        mapManager?.let {
                            onLayersVisibility(it.updateLayersVisibility(layerVisibility))
                        }
                        dismiss()
                    }
                }
            }
        }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet,
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
                    com.google.android.material.R.id.design_bottom_sheet,
                ) as FrameLayout?
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            behavior.addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(
                        bottomSheet: View,
                        newState: Int,
                    ) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(
                        bottomSheet: View,
                        slideOffset: Float,
                    ) {}
                },
            )
        }
        return dialog
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dismiss()
    }

    fun setOnLayersVisibilityListener(listener: (HashMap<String, MapLayer>) -> Unit): MapLayerDialog {
        this.onLayersVisibility = listener
        return this
    }

    companion object {
        private const val ARG_PROGRAM_UID = "programUid"

        fun newInstance(programUid: String?): MapLayerDialog =
            MapLayerDialog().apply {
                arguments =
                    Bundle().apply {
                        putString(ARG_PROGRAM_UID, programUid)
                    }
            }
    }

    private fun getMapLayer(): LinkedHashMap<String, MutableList<MapLayerItem>> {
        val layerMap =
            linkedMapOf<String, MutableList<MapLayerItem>>(
                "TEI" to mutableListOf(),
                "ENROLLMENT" to mutableListOf(),
                "TRACKER_EVENT" to mutableListOf(),
                "RELATIONSHIP" to mutableListOf(),
                "EVENT" to mutableListOf(),
                "DE" to mutableListOf(),
                "HEATMAP" to mutableListOf(),
            )

        mapManager?.mapLayerManager?.mapLayers?.toSortedMap()?.forEach { (source, layer) ->
            layerVisibility[source] ?: run { layerVisibility[source] = layer.visible }
            when (layer) {
                is TeiMapLayer ->
                    layerMap["TEI"]?.add(
                        MapLayerItem(
                            source,
                            requireContext().getString(R.string.dialog_layer_tei_coordinates),
                            MapLayerManager.TEI_ICON_ID,
                        ),
                    )

                is EnrollmentMapLayer ->
                    layerMap["ENROLLMENT"]?.add(
                        MapLayerItem(
                            source,
                            resourceManager.formatWithEnrollmentLabel(
                                programUid = programUid,
                                stringResource = R.string.dialog_layer_enrollment_coordinates_v2,
                                1,
                            ),
                            MapLayerManager.ENROLLMENT_ICON_ID,
                        ),
                    )

                is TeiEventMapLayer ->
                    layerMap["TRACKER_EVENT"]?.add(
                        MapLayerItem(source, image = "${MapLayerManager.STAGE_ICON_ID}_$source"),
                    )

                is HeatmapMapLayer ->
                    layerMap["HEATMAP"]?.add(
                        MapLayerItem(
                            source,
                            requireContext().getString(R.string.dialog_layer_heatmap),
                            HEATMAP_ICON,
                        ),
                    )

                is RelationshipMapLayer ->
                    layerMap["RELATIONSHIP"]?.add(
                        MapLayerItem(source, null, "${RELATIONSHIP_ICON}_$source"),
                    )

                is EventMapLayer ->
                    layerMap["EVENT"]?.add(
                        MapLayerItem(
                            source,
                            requireContext().getString(R.string.dialog_layer_event),
                            EventMapManager.ICON_ID,
                        ),
                    )

                is FieldMapLayer ->
                    layerMap["DE"]?.add(
                        MapLayerItem(
                            source,
                            mapManager?.getLayerName(source),
                            "${EventMapManager.DE_ICON_ID}_$source",
                        ),
                    )
            }
        }
        return layerMap
    }

    @Composable
    private fun MapLayerList(
        mapLayersMap: LinkedHashMap<String, MutableList<MapLayerItem>>,
        layerVisibility: HashMap<String, Boolean>,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val mapLayers = mutableListOf<MapLayerItem>()
            mapLayersMap.forEach { mapLayers.addAll(it.value) }
            items(
                items = mapLayers,
                key = { mapLayer -> mapLayer.hashCode() },
            ) { item ->
                MapLayerCheckbox(item, layerVisibility) {
                    layerVisibility[item.source] = it
                }
            }
        }
    }

    @Composable
    private fun MapLayerCheckbox(
        item: MapLayerItem,
        layerVisibility: java.util.HashMap<String, Boolean>,
        onCheckedChange: (Boolean) -> Unit = {},
    ) {
        var isChecked by remember { mutableStateOf(layerVisibility[item.source] ?: false) }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    onCheckedChange(it)
                    isChecked = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colorResource(id = R.color.colorPrimary)),
            )

            Text(
                modifier = Modifier.weight(1f),
                text = item.text ?: item.source,
            )

            item.image?.let {
                val bitmap =
                    if (it == HEATMAP_ICON) {
                        BitmapFactory.decodeResource(
                            LocalContext.current.resources,
                            R.drawable.ic_heatmap_icon,
                        )
                    } else {
                        mapManager
                            ?.mapLayerManager
                            ?.maplibreMap
                            ?.style
                            ?.getImage(item.image)
                    }

                bitmap?.let { bmp ->
                    Image(
                        modifier = Modifier.size(24.dp),
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                    )
                    Spacer(Modifier.padding(end = 8.dp))
                }
            }
        }
    }
}
