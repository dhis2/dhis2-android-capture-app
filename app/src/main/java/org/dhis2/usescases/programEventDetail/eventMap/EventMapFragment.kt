package org.dhis2.usescases.programEventDetail.eventMap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.activityViewModels
import com.mapbox.mapboxsdk.maps.MapView
import org.dhis2.R
import org.dhis2.commons.bindings.launchImageDetail
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.ui.SyncButtonProvider
import org.dhis2.maps.camera.centerCameraOnFeatures
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.managers.EventMapManager
import org.dhis2.maps.views.MapScreen
import org.dhis2.maps.views.OnMapClickListener
import org.dhis2.ui.avatar.AvatarProvider
import org.dhis2.ui.theme.Dhis2Theme
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import javax.inject.Inject

class EventMapFragment :
    FragmentGlobalAbstract(),
    EventMapFragmentView {

    @Inject
    lateinit var mapNavigation: org.dhis2.maps.ExternalMapNavigation

    private var eventMapManager: EventMapManager? = null

    private val programEventsViewModel: ProgramEventDetailViewModel by activityViewModels()

    @Inject
    lateinit var presenter: EventMapPresenter

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as ProgramEventDetailActivity).component
            ?.plus(EventMapModule(this))
            ?.inject(this)
        programEventsViewModel.setProgress(true)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Dhis2Theme {
                    val listState = rememberLazyListState()
                    val eventMapData by presenter.eventMapData.observeAsState(initial = null)
                    val items by remember {
                        derivedStateOf { eventMapData?.mapItems ?: emptyList() }
                    }

                    val clickedItem by presenter.mapItemClicked.observeAsState(initial = null)

                    LaunchedEffect(key1 = clickedItem) {
                        clickedItem?.let {
                            listState.animateScrollToItem(
                                items.indexOfFirst { it.uid == clickedItem },
                            )
                        }
                    }

                    LaunchedEffect(key1 = items) {
                        eventMapData?.let { data ->
                            eventMapManager?.takeIf { it.isMapReady() }?.update(
                                data.featureCollectionMap,
                                data.boundingBox,
                            )
                        }
                    }

                    MapScreen(
                        items = items,
                        listState = listState,
                        onItemScrolled = { item ->
                            with(eventMapManager) {
                                this?.requestMapLayerManager()?.selectFeature(null)
                                this?.findFeatures(item.uid)
                                    ?.takeIf { it.isNotEmpty() }?.let { features ->
                                        map?.centerCameraOnFeatures(features)
                                    }
                            }
                        },
                        onNavigate = { item ->
                            eventMapManager?.findFeature(item.uid)?.let { feature ->
                                startActivity(mapNavigation.navigateToMapIntent(feature))
                            }
                        },
                        actionButtons = {
                            IconButton(
                                style = IconButtonStyle.TONAL,
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_layers),
                                        contentDescription = "",
                                    )
                                },
                            ) {
                                MapLayerDialog(eventMapManager!!) { layersVisibility ->
                                    presenter.filterVisibleMapItems(layersVisibility)
                                }.show(
                                    childFragmentManager,
                                    MapLayerDialog::class.java.name,
                                )
                            }

                            IconButton(
                                style = IconButtonStyle.TONAL,
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_my_location),
                                        contentDescription = "",
                                    )
                                },
                                onClick = ::onLocationButtonClicked,
                            )
                        },
                        map = {
                            AndroidView(
                                factory = { context ->
                                    MapView(context).also {
                                        loadMap(it, savedInstanceState)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("MAP"),
                            ) {}
                        },
                        onItem = { item ->
                            ListCard(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .testTag("MAP_ITEM"),
                                listAvatar = {
                                    AvatarProvider(
                                        avatarProviderConfiguration = item.avatarProviderConfiguration,
                                        onImageClick = ::launchImageDetail,
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
                                    SyncButtonProvider(state = item.state) {
                                        programEventsViewModel.eventSyncClicked.value = item.uid
                                    }
                                },
                                expandLabelText = stringResource(id = R.string.show_more),
                                shrinkLabelText = stringResource(id = R.string.show_less),
                                onCardClick = {
                                    programEventsViewModel.eventClicked.value =
                                        Pair(item.uid, "")
                                },
                            )
                        },
                    )
                }
            }
        }
    }

    private fun onLocationButtonClicked() {
        eventMapManager?.onLocationButtonClicked(
            locationProvider.hasLocationEnabled(),
            requireActivity(),
        )
    }

    override fun onResume() {
        super.onResume()
        programEventsViewModel.updateEvent?.let { eventUid ->
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        eventMapManager?.permissionsManager?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
        )
    }

    private fun loadMap(mapView: MapView, savedInstanceState: Bundle?) {
        eventMapManager = EventMapManager(mapView).also {
            lifecycle.addObserver(it)
            it.onCreate(savedInstanceState)
            it.featureType = presenter.programFeatureType()
            it.onMapClickListener = OnMapClickListener(it, presenter::onFeatureClicked)
            it.init(
                mapStyles = programEventsViewModel.fetchMapStyles(),
                onInitializationFinished = {
                    presenter.filterVisibleMapItems(
                        it.mapLayerManager.mapLayers.toMap(),
                    )
                    presenter.init()
                },
                onMissingPermission = { permissionsManager ->
                    if (locationProvider.hasLocationEnabled()) {
                        permissionsManager?.requestLocationPermissions(requireActivity())
                    } else {
                        LocationSettingLauncher.requestEnableLocationSetting(requireContext())
                    }
                },
            )
        }
    }

    override fun updateEventCarouselItem(programEventViewModel: ProgramEventViewModel) {
        programEventsViewModel.setProgress(false)
        programEventsViewModel.updateEvent = null
    }
}
