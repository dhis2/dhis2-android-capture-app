package org.dhis2.usescases.searchTrackEntity.mapView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.io.File
import javax.inject.Inject
import org.dhis2.Bindings.dp
import org.dhis2.animations.CarouselViewAnimations
import org.dhis2.commons.bindings.clipWithRoundedCorners
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.FragmentSearchMapBinding
import org.dhis2.maps.ExternalMapNavigation
import org.dhis2.maps.carousel.CarouselAdapter
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.managers.TeiMapManager
import org.dhis2.maps.model.MapStyle
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchList
import org.dhis2.usescases.searchTrackEntity.SearchScreenState
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.isPortrait

const val ARG_FROM_RELATIONSHIP = "ARG_FROM_RELATIONSHIP"
const val ARG_TE_TYPE = "ARG_TE_TYPE"

class SearchTEMap : FragmentGlobalAbstract(), MapboxMap.OnMapClickListener {

    @Inject
    lateinit var mapNavigation: ExternalMapNavigation

    @Inject
    lateinit var presenter: SearchTEContractsModule.Presenter

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    @Inject
    lateinit var animations: CarouselViewAnimations

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    private var teiMapManager: TeiMapManager? = null
    private var carouselAdapter: CarouselAdapter? = null
    lateinit var binding: FragmentSearchMapBinding

    private val fromRelationship by lazy {
        arguments?.getBoolean(ARG_FROM_RELATIONSHIP) ?: false
    }

    private val tEType by lazy {
        arguments?.getString(ARG_TE_TYPE)
    }

    companion object {
        fun get(fromRelationships: Boolean, teType: String): SearchTEMap {
            return SearchTEMap().apply {
                arguments = bundleArguments(fromRelationships, teType)
            }
        }
    }

    private fun bundleArguments(fromRelationships: Boolean, teType: String): Bundle {
        return Bundle().apply {
            putBoolean(ARG_FROM_RELATIONSHIP, fromRelationships)
            putString(ARG_TE_TYPE, teType)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as SearchTEActivity).searchComponent.plus(
            SearchTEMapModule()
        ).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchMapBinding.inflate(inflater, container, false)

        binding.mapLayerButton.setOnClickListener {
            MapLayerDialog(teiMapManager!!)
                .show(childFragmentManager, MapLayerDialog::class.java.name)
        }

        binding.mapPositionButton.setOnClickListener {
            if (locationProvider.hasLocationEnabled()) {
                teiMapManager?.centerCameraOnMyPosition { permissionManager ->
                    permissionManager?.requestLocationPermissions(requireActivity())
                }
            } else {
                LocationSettingLauncher.requestEnableLocationSetting(requireContext())
            }
        }

        binding.openSearchButton.setOnClickListener {
            viewModel.setSearchScreen()
        }

        teiMapManager = TeiMapManager(binding.mapView)
        teiMapManager?.let { lifecycle.addObserver(it) }
        teiMapManager?.onCreate(savedInstanceState)
        teiMapManager?.teiFeatureType = presenter.getTrackedEntityType(tEType).featureType()
        teiMapManager?.enrollmentFeatureType =
            if (presenter.program != null) presenter.program.featureType() else null
        teiMapManager?.onMapClickListener = this
        teiMapManager?.mapStyle =
            MapStyle(
                presenter.teiColor,
                presenter.symbolIcon,
                presenter.enrollmentColor,
                presenter.enrollmentSymbolIcon,
                presenter.programStageStyle,
                ColorUtils.getPrimaryColor(
                    requireContext(),
                    ColorUtils.ColorType.PRIMARY_DARK
                )
            )
        initializeCarousel()
        teiMapManager?.init(
            viewModel.fetchMapStyles(),
            onInitializationFinished = {
                presenter.getMapData()

                observeMapResults()

                viewModel.fetchMapResults()
            },
            onMissingPermission = { permissionsManager ->
                permissionsManager?.requestLocationPermissions(requireActivity())
            }
        )
        binding.content.clipWithRoundedCorners()

        viewModel.screenState.observe(viewLifecycleOwner) {
            if (it.screenState == SearchScreenState.MAP) {
                val backdropActive = isPortrait() && (it as SearchList).searchFilters.isOpened
                binding.mapView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    val bottomMargin = if (backdropActive) {
                        0
                    } else {
                        40.dp
                    }
                    setMargins(0, 0, 0, bottomMargin)
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        teiMapManager?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        teiMapManager?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        teiMapManager?.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        teiMapManager?.permissionsManager?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    private fun observeMapResults() {
        animations.initMapLoading(binding.mapCarousel)

        viewModel.mapResults.removeObservers(viewLifecycleOwner)
        viewModel.mapResults.observe(viewLifecycleOwner) { trackerMapData ->
            teiMapManager?.update(
                trackerMapData.teiFeatures,
                trackerMapData.eventFeatures,
                trackerMapData.dataElementFeaturess,
                trackerMapData.teiBoundingBox
            )
            carouselAdapter?.setAllItems(trackerMapData.allItems())
            carouselAdapter?.updateLayers(teiMapManager?.mapLayerManager?.mapLayers)
            animations.endMapLoading(binding.mapCarousel)
            viewModel.mapDataFetched()
        }
    }

    private fun initializeCarousel() {
        carouselAdapter = CarouselAdapter.Builder()
            .addOnTeiClickListener { teiUid: String, enrollmentUid: String?, isDeleted: Boolean? ->
                if (binding.mapCarousel.carouselEnabled) {
                    if (fromRelationship) {
                        presenter.addRelationship(
                            teiUid,
                            null,
                            NetworkUtils.isOnline(requireContext())
                        )
                    } else {
                        presenter.onTEIClick(teiUid, enrollmentUid, isDeleted!!)
                    }
                }
                true
            }
            .addOnSyncClickListener { teiUid: String? ->
                if (binding.mapCarousel.carouselEnabled) {
                    presenter.onSyncIconClick(teiUid)
                }
                true
            }
            .addOnDeleteRelationshipListener { relationshipUid: String? ->
                if (binding.mapCarousel.carouselEnabled) {
                    presenter.deleteRelationship(relationshipUid)
                    viewModel.refreshData()
                }
                true
            }
            .addOnRelationshipClickListener { teiUid: String?, ownerType: RelationshipOwnerType ->
                if (binding.mapCarousel.carouselEnabled) {
                    presenter.onTEIClick(teiUid, null, false)
                }
                true
            }
            .addOnEventClickListener { teiUid: String?, enrollmentUid: String?, eventUid: String? ->
                if (binding.mapCarousel.carouselEnabled) {
                    presenter.onTEIClick(teiUid, enrollmentUid, false)
                }
                true
            }
            .addOnProfileImageClickListener { path: String? ->
                if (binding.mapCarousel.carouselEnabled) {
                    ImageDetailBottomDialog(
                        null,
                        File(path)
                    ).show(
                        childFragmentManager,
                        ImageDetailBottomDialog.TAG
                    )
                }
                Unit
            }
            .addOnNavigateClickListener { uuid: String? ->
                val feature = teiMapManager!!.findFeature(
                    uuid!!
                )
                if (feature != null) {
                    startActivity(mapNavigation.navigateToMapIntent(feature))
                }
                Unit
            }
            .addProgram(presenter.program)
            .addMapManager(teiMapManager!!)
            .build()
        teiMapManager?.carouselAdapter = carouselAdapter
        binding.mapCarousel.setAdapter(carouselAdapter)
        teiMapManager?.let { binding.mapCarousel.attachToMapManager(it) }
    }

    override fun onMapClick(point: LatLng): Boolean {
        val featureFound = teiMapManager!!.markFeatureAsSelected(point, null)
        if (featureFound != null) {
            binding.mapCarousel.scrollToFeature(featureFound)
            return true
        }
        return false
    }
}
