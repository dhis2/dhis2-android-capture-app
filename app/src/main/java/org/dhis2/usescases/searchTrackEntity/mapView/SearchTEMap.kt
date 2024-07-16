package org.dhis2.usescases.searchTrackEntity.mapView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.R
import org.dhis2.animations.CarouselViewAnimations
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.FragmentSearchMapBinding
import org.dhis2.maps.ExternalMapNavigation
import org.dhis2.maps.camera.centerCameraOnFeatures
import org.dhis2.maps.carousel.CarouselAdapter
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.managers.TeiMapManager
import org.dhis2.maps.model.MapStyle
import org.dhis2.maps.views.MapScreen
import org.dhis2.ui.avatar.AvatarProvider
import org.dhis2.ui.theme.Dhis2Theme
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchList
import org.dhis2.usescases.searchTrackEntity.SearchScreenState
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.isPortrait
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import javax.inject.Inject
import org.dhis2.bindings.dp as DP

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

    @Inject
    lateinit var colorUtils: ColorUtils

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
            SearchTEMapModule(),
        ).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchMapBinding.inflate(inflater, container, false)

        viewModel.screenState.observe(viewLifecycleOwner) {
            if (it.screenState == SearchScreenState.MAP) {
                val backdropActive = isPortrait() && (it as SearchList).searchFilters.isOpened
                binding.mapView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    val bottomMargin = if (backdropActive) {
                        0
                    } else {
                        40.DP
                    }
                    setMargins(0, 0, 0, bottomMargin)
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindow,
            )
            setContent {
                val listState = rememberLazyListState()

                val trackerMapData by viewModel.mapResults.collectAsState(initial = null)
                val items by remember {
                    derivedStateOf { trackerMapData?.mapItems ?: emptyList() }
                }

                val clickedItem by viewModel.mapItemClicked.collectAsState(initial = null)

                LaunchedEffect(key1 = clickedItem) {
                    if (clickedItem != null) {
                        listState.animateScrollToItem(
                            items.indexOfFirst { it.uid == clickedItem },
                        )
                    }
                }

                LaunchedEffect(key1 = items) {
                    trackerMapData?.let { data ->
                        teiMapManager?.takeIf { it.isMapReady() }?.update(
                            data.teiFeatures,
                            data.eventFeatures,
                            data.dataElementFeaturess,
                            data.teiBoundingBox,
                        )
                        viewModel.mapDataFetched()
                    }
                }

                Dhis2Theme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .padding(bottom = 50.dp),
                    ) {
                        MapScreen(
                            items = items,
                            listState = listState,
                            map = {
                                AndroidView(factory = { context ->
                                    val map = MapView(context)
                                    loadMap(map, savedInstanceState)
                                    map
                                }) {
                                }
                            },
                            actionButtons = {
                                IconButton(
                                    style = IconButtonStyle.TONAL,
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_search),
                                            contentDescription = "",
                                        )
                                    },
                                ) {
                                    viewModel.setSearchScreen()
                                }
                                IconButton(
                                    style = IconButtonStyle.TONAL,
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_layers),
                                            contentDescription = "",
                                        )
                                    },
                                ) {
                                    MapLayerDialog(teiMapManager!!) { layersVisibility ->
                                        viewModel.filterVisibleMapItems(layersVisibility)
                                    }
                                        .show(childFragmentManager, MapLayerDialog::class.java.name)
                                }
                                IconButton(
                                    style = IconButtonStyle.TONAL,
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_my_location),
                                            contentDescription = "",
                                        )
                                    },
                                ) {
                                    if (locationProvider.hasLocationEnabled()) {
                                        teiMapManager?.centerCameraOnMyPosition { permissionManager ->
                                            permissionManager?.requestLocationPermissions(
                                                requireActivity(),
                                            )
                                        }
                                    } else {
                                        LocationSettingLauncher.requestEnableLocationSetting(
                                            requireContext(),
                                        )
                                    }
                                }
                            },
                            onItemScrolled = { item ->
                                with(teiMapManager) {
                                    this?.requestMapLayerManager()?.selectFeature(null)
                                    this?.findFeatures(item.uid)
                                        ?.takeIf { it.isNotEmpty() }?.let { features ->
                                            map?.centerCameraOnFeatures(features)
                                        }
                                }
                            },
                            onNavigate = { item ->
                                teiMapManager?.findFeature(item.uid)?.let { feature ->
                                    startActivity(mapNavigation.navigateToMapIntent(feature))
                                }
                            },
                            onItem = { item ->
                                ListCard(
                                    modifier = Modifier.fillParentMaxWidth(),
                                    listAvatar = {
                                        AvatarProvider(
                                            avatarProviderConfiguration = item.avatarProviderConfiguration,
                                            onImageClick = { path ->
                                                if (path.isNotBlank()) {
                                                    val intent = ImageDetailActivity.intent(
                                                        context = requireContext(),
                                                        title = null,
                                                        imagePath = path,
                                                    )

                                                    startActivity(intent)
                                                }
                                            },
                                        )
                                    },
                                    title = ListCardTitleModel(text = item.title),
                                    description = item.description?.let {
                                        ListCardDescriptionModel(
                                            text = it,
                                        )
                                    },
                                    lastUpdated = item.lastUpdated,
                                    additionalInfoList = item.additionalInfoList,
                                    actionButton = {
                                    },
                                    expandLabelText = stringResource(id = R.string.show_more),
                                    shrinkLabelText = stringResource(id = R.string.show_less),
                                    onCardClick = {
                                        if (fromRelationship) {
                                            viewModel.onAddRelationship(
                                                item.uid,
                                                item.relatedInfo?.relationship?.relationshipTypeUid,
                                                item.isOnline,
                                            )
                                        } else {
                                            viewModel.onTeiClick(
                                                item.uid,
                                                item.relatedInfo?.enrollment?.uid,
                                                item.isOnline,
                                            )
                                        }
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    private fun loadMap(mapView: MapView, savedInstanceState: Bundle?) {
        teiMapManager = TeiMapManager(mapView)
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
                colorUtils.getPrimaryColor(
                    requireContext(),
                    ColorType.PRIMARY_DARK,
                ),
            )
        teiMapManager?.init(
            viewModel.fetchMapStyles(),
            onInitializationFinished = {
                presenter.getMapData()
                observeMapResults()
                viewModel.filterVisibleMapItems(
                    teiMapManager?.mapLayerManager?.mapLayers?.toMap() ?: emptyMap(),
                )
//                viewModel.fetchMapResults()
            },
            onMissingPermission = { permissionsManager ->
                permissionsManager?.requestLocationPermissions(requireActivity())
            },
        )
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        teiMapManager?.permissionsManager?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
        )
    }

    private fun observeMapResults() {
//        animations.initMapLoading(binding.mapCarousel)

//        viewModel.mapResults.removeObservers(viewLifecycleOwner)
        /*viewModel.mapResults.observe(viewLifecycleOwner) { trackerMapData ->
            teiMapManager?.update(
                trackerMapData.teiFeatures,
                trackerMapData.eventFeatures,
                trackerMapData.dataElementFeaturess,
                trackerMapData.teiBoundingBox,
            )
            carouselAdapter?.setAllItems(trackerMapData.allItems())
            carouselAdapter?.updateLayers(teiMapManager?.mapLayerManager?.mapLayers)
            animations.endMapLoading(binding.mapCarousel)
            viewModel.mapDataFetched()
        }*/
    }

    private fun initializeCarousel() {
        carouselAdapter = CarouselAdapter.Builder()
            .addOnTeiClickListener { teiUid: String, enrollmentUid: String?, isDeleted: Boolean? ->
                if (binding.mapCarousel.carouselEnabled) {
                    if (fromRelationship) {
                        presenter.addRelationship(
                            teiUid,
                            null,
                            NetworkUtils.isOnline(requireContext()),
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
                if (binding.mapCarousel.carouselEnabled && !path.isNullOrBlank()) {
                    val intent = ImageDetailActivity.intent(
                        context = requireContext(),
                        title = null,
                        imagePath = path,
                    )

                    startActivity(intent)
                }
                Unit
            }
            .addOnNavigateClickListener { uuid: String? ->
                val feature = teiMapManager!!.findFeature(
                    uuid!!,
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
            viewModel.onFeatureClicked(featureFound)
            binding.mapCarousel.scrollToFeature(featureFound)
            return true
        }
        return false
    }
}
