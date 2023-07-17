package org.dhis2.usescases.programEventDetail.eventMap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import javax.inject.Inject
import org.dhis2.Bindings.dp
import org.dhis2.animations.CarouselViewAnimations
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.databinding.FragmentProgramEventDetailMapBinding
import org.dhis2.maps.carousel.CarouselAdapter
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel
import org.dhis2.usescases.programEventDetail.ProgramEventMapData

class EventMapFragment :
    FragmentGlobalAbstract(),
    EventMapFragmentView,
    MapboxMap.OnMapClickListener {

    private lateinit var binding: FragmentProgramEventDetailMapBinding

    @Inject
    lateinit var animations: CarouselViewAnimations

    @Inject
    lateinit var mapNavigation: org.dhis2.maps.ExternalMapNavigation

    private var eventMapManager: org.dhis2.maps.managers.EventMapManager? = null

    private val fragmentLifeCycle = lifecycle

    private val programEventsViewModel: ProgramEventDetailViewModel by activityViewModels()

    @Inject
    lateinit var presenter: EventMapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as ProgramEventDetailActivity).component
            ?.plus(EventMapModule(this))
            ?.inject(this)
        programEventsViewModel.setProgress(true)
        binding = FragmentProgramEventDetailMapBinding.inflate(inflater, container, false)
        binding.apply {
            eventMapManager = org.dhis2.maps.managers.EventMapManager(mapView)
            eventMapManager?.let { fragmentLifeCycle.addObserver(it) }
            eventMapManager?.onCreate(savedInstanceState)
            eventMapManager?.featureType = presenter.programFeatureType()
            eventMapManager?.onMapClickListener = this@EventMapFragment
            eventMapManager?.init(
                mapStyles = programEventsViewModel.fetchMapStyles(),
                onInitializationFinished = {
                    presenter.init()
                },
                onMissingPermission = { permissionsManager ->
                    if (locationProvider.hasLocationEnabled()) {
                        permissionsManager?.requestLocationPermissions(requireActivity())
                    } else {
                        LocationSettingLauncher.requestEnableLocationSetting(requireContext())
                    }
                }
            )
            mapLayerButton.setOnClickListener {
                eventMapManager?.let {
                    MapLayerDialog(it)
                        .show(childFragmentManager, MapLayerDialog::class.java.name)
                }
            }

            mapPositionButton.setOnClickListener {
                if (locationProvider.hasLocationEnabled()) {
                    eventMapManager?.centerCameraOnMyPosition { permissionManager ->
                        permissionManager?.requestLocationPermissions(requireActivity())
                    }
                } else {
                    LocationSettingLauncher.requestEnableLocationSetting(requireContext())
                }
            }
        }

        programEventsViewModel.backdropActive.observe(viewLifecycleOwner) { backdropActive ->
            binding.mapView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                val bottomMargin = if (backdropActive) {
                    0
                } else {
                    40.dp
                }
                setMargins(0, 0, 0, bottomMargin)
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
                    .addMapManager(eventMapManager!!)
                    .build()
            binding.mapCarousel.setAdapter(carouselAdapter)
            eventMapManager?.carouselAdapter = carouselAdapter
            eventMapManager?.let { binding.mapCarousel.attachToMapManager(eventMapManager!!) }
            carouselAdapter.setAllItems(mapData.events)
            carouselAdapter.updateLayers(eventMapManager?.mapLayerManager?.mapLayers)
        } else {
            eventMapManager?.let {
                (binding.mapCarousel.adapter as CarouselAdapter?)?.setItems(mapData.events)
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
        eventMapManager?.markFeatureAsSelected(point, null)?.let {
            binding.mapCarousel.scrollToFeature(it)
            return true
        } ?: return false
    }
}
