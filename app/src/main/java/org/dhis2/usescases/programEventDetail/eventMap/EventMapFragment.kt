package org.dhis2.usescases.programEventDetail.eventMap

import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.util.ArrayList
import javax.inject.Inject
import org.dhis2.animations.CarouselViewAnimations
import org.dhis2.databinding.FragmentProgramEventDetailMapBinding
import org.dhis2.uicomponents.map.ExternalMapNavigation
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.layer.LayerType
import org.dhis2.uicomponents.map.layer.MapLayerDialog
import org.dhis2.uicomponents.map.managers.EventMapManager
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel
import org.dhis2.usescases.programEventDetail.ProgramEventMapData
import org.dhis2.usescases.programEventDetail.ProgramEventViewModel

class EventMapFragment :
    FragmentGlobalAbstract(),
    EventMapFragmentView,
    MapboxMap.OnMapClickListener {

    private lateinit var binding: FragmentProgramEventDetailMapBinding

    @Inject
    lateinit var animations: CarouselViewAnimations

    @Inject
    lateinit var mapNavigation: ExternalMapNavigation

    private var eventMapManager: EventMapManager? = null

    private val fragmentLifeCycle = lifecycle

    private val programEventsViewModel by lazy {
        ViewModelProviders.of(requireActivity())[ProgramEventDetailViewModel::class.java]
    }

    @Inject
    lateinit var presenter: EventMapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as ProgramEventDetailActivity).component.plus(EventMapModule(this)).inject(this)
        programEventsViewModel.setProgress(true)
        binding = FragmentProgramEventDetailMapBinding.inflate(inflater, container, false)
        binding.apply {
            eventMapManager = EventMapManager(mapView)
            eventMapManager?.let { fragmentLifeCycle.addObserver(it) }
            eventMapManager?.onCreate(savedInstanceState)
            eventMapManager?.featureType = presenter.programFeatureType()
            eventMapManager?. onMapClickListener = this@EventMapFragment
            eventMapManager?.init(
                onInitializationFinished = {
                    presenter.init()
                },
                onMissingPermission = { permissionsManager ->
                    permissionsManager?.requestLocationPermissions(requireActivity())
                }
            )
            mapLayerButton.setOnClickListener {
                eventMapManager?.let {
                    MapLayerDialog(it)
                        .show(childFragmentManager, MapLayerDialog::class.java.name)
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        programEventsViewModel.updateEvent?.let { eventUid ->
            animations.initMapLoading(binding.mapCarousel)
            programEventsViewModel.setProgress(true)
            presenter.getEventInfo(eventUid)
        }
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        eventMapManager?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        eventMapManager?.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        eventMapManager?.permissionsManager?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    override fun setMap(mapData: ProgramEventMapData) {
        eventMapManager?.update(
            mapData.featureCollectionMap,
            mapData.boundingBox
        )
        if (binding.mapCarousel.adapter == null) {
            val carouselAdapter =
                CarouselAdapter.Builder()
                    .addOnSyncClickListener { teiUid: String? ->
                        if (binding.mapCarousel.carouselEnabled) {
                            programEventsViewModel.eventSyncClicked.value = teiUid
                        }
                        true
                    }
                    .addOnEventClickListener { teiUid: String?, orgUnit: String?, _: String? ->
                        if (binding.mapCarousel.carouselEnabled) {
                            programEventsViewModel.eventClicked.value = Pair(teiUid!!, orgUnit!!)
                        }
                        true
                    }
                    .addOnNavigateClickListener { uid ->
                        eventMapManager?.findFeature(uid)?.let { feature ->
                            startActivity(mapNavigation.navigateToMapIntent(feature))
                        }
                    }
                    .build()
            binding.mapCarousel.setAdapter(carouselAdapter)
            eventMapManager?.let { binding.mapCarousel.attachToMapManager(eventMapManager!!) }
            carouselAdapter.addItems(mapData.events)
        } else {
            eventMapManager?.let {
                (binding.mapCarousel.adapter as CarouselAdapter?)!!.setItems(mapData.events)
            }
        }

        eventMapManager?.mapLayerManager?.selectFeature(null)

        animations.endMapLoading(binding.mapCarousel)
        programEventsViewModel.setProgress(false)
    }

    override fun updateEventCarouselItem(programEventViewModel: ProgramEventViewModel) {
        (binding.mapCarousel.adapter as CarouselAdapter).updateItem(programEventViewModel)
        animations.endMapLoading(binding.mapCarousel)
        programEventsViewModel.setProgress(false)
        programEventsViewModel.updateEvent = null
    }

    override fun onMapClick(point: LatLng): Boolean {
        val rectF = eventMapManager?.map?.projection?.toScreenLocation(point)?.let { pointf ->
            RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)
        }

        val features: MutableList<Feature> = ArrayList()
        rectF?.let {
            for (mapLayer in eventMapManager?.mapLayerManager?.getLayers() ?: emptyList()) {
                eventMapManager?.map?.queryRenderedFeatures(it, mapLayer.getId())
                    ?.let { layerFeatures ->
                        features.addAll(layerFeatures)
                    }
            }
        }

        if (features.isNotEmpty()) {
            val selectedFeature = eventMapManager?.findFeature(
                LayerType.EVENT_LAYER.name,
                MapEventToFeatureCollection.EVENT,
                features[0].getStringProperty(MapEventToFeatureCollection.EVENT)
            )
            eventMapManager?.mapLayerManager?.getLayer(
                LayerType.EVENT_LAYER.name,
                true
            )?.setSelectedItem(selectedFeature)
            binding.mapCarousel.scrollToFeature(features[0])
            return true
        }
        return false
    }
}
