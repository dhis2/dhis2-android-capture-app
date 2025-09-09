package org.dhis2.usescases.searchTrackEntity.mapView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.activityViewModels
import org.dhis2.R
import org.dhis2.commons.bindings.launchImageDetail
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.ui.SyncButtonProvider
import org.dhis2.maps.ExternalMapNavigation
import org.dhis2.maps.camera.centerCameraOnFeatures
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.location.MapLocationEngine
import org.dhis2.maps.managers.TeiMapManager
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.MapStyle
import org.dhis2.maps.views.LocationIcon
import org.dhis2.maps.views.MapScreen
import org.dhis2.maps.views.OnMapClickListener
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.files.buildPainterForFile
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.maplibre.android.maps.MapView
import javax.inject.Inject

const val ARG_FROM_RELATIONSHIP = "ARG_FROM_RELATIONSHIP"
const val ARG_TE_TYPE = "ARG_TE_TYPE"

class SearchTEMap : FragmentGlobalAbstract() {
    @Inject
    lateinit var mapNavigation: ExternalMapNavigation

    @Inject
    lateinit var presenter: SearchTEContractsModule.Presenter

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    @Inject
    lateinit var colorUtils: ColorUtils

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    private var teiMapManager: TeiMapManager? = null

    private val fromRelationship by lazy {
        arguments?.getBoolean(ARG_FROM_RELATIONSHIP) ?: false
    }

    private val tEType by lazy {
        arguments?.getString(ARG_TE_TYPE)
    }

    companion object {
        fun get(
            fromRelationships: Boolean,
            teType: String,
        ): SearchTEMap =
            SearchTEMap().apply {
                arguments = bundleArguments(fromRelationships, teType)
            }
    }

    private fun bundleArguments(
        fromRelationships: Boolean,
        teType: String,
    ): Bundle =
        Bundle().apply {
            putBoolean(ARG_FROM_RELATIONSHIP, fromRelationships)
            putString(ARG_TE_TYPE, teType)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as SearchTEActivity)
            .searchComponent
            ?.plus(
                SearchTEMapModule(),
            )?.inject(this)
        viewModel.setMapScreen()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
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

                val locationState = teiMapManager?.locationState?.collectAsState()

                val mapDataFinishedLoading = teiMapManager?.dataFinishedLoading?.collectAsState()

                LaunchedEffect(key1 = clickedItem) {
                    clickedItem?.let {
                        listState.animateScrollToItem(
                            items.indexOfFirst { it.uid == clickedItem },
                        )
                    }
                }

                LaunchedEffect(key1 = items) {
                    trackerMapData?.let { data ->
                        teiMapManager
                            ?.takeIf { it.isMapReady() }
                            ?.update(
                                data.teiFeatures,
                                data.eventFeatures,
                                data.dataElementFeaturess,
                                data.teiBoundingBox,
                            ).also {
                                viewModel.mapManager = teiMapManager
                            }
                    }
                }

                DHIS2Theme {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
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
                                            tint = TextColor.OnPrimaryContainer,
                                        )
                                    },
                                ) {
                                    viewModel.setSearchScreen()
                                }
                                mapDataFinishedLoading?.let {
                                    if (it.value) {
                                        IconButton(
                                            style = IconButtonStyle.TONAL,
                                            icon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_layers),
                                                    contentDescription = "",
                                                    tint = TextColor.OnPrimaryContainer,
                                                )
                                            },
                                        ) {
                                            val mapLayerDialog =
                                                MapLayerDialog.newInstance(viewModel.initialProgramUid)
                                            mapLayerDialog.mapManager = viewModel.mapManager
                                            mapLayerDialog.setOnLayersVisibilityListener { layersVisibility ->
                                                viewModel.filterVisibleMapItems(layersVisibility)
                                            }
                                            mapLayerDialog.show(
                                                childFragmentManager,
                                                MapLayerDialog::class.java.name,
                                            )
                                        }
                                    }
                                }
                                locationState?.let {
                                    LocationIcon(
                                        locationState = it.value,
                                        onLocationButtonClicked = ::onLocationButtonClicked,
                                    )
                                }
                            },
                            onItemScrolled = { item ->
                                with(teiMapManager) {
                                    this?.requestMapLayerManager()?.selectFeature(null)
                                    this
                                        ?.findFeatures(item.uid)
                                        ?.takeIf { it.isNotEmpty() }
                                        ?.let { features ->
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
                                    modifier =
                                        Modifier
                                            .fillParentMaxWidth()
                                            .testTag("MAP_ITEM"),
                                    listCardState =
                                        rememberListCardState(
                                            title =
                                                ListCardTitleModel(
                                                    text = item.title,
                                                    allowOverflow = false,
                                                ),
                                            description =
                                                item.description?.let {
                                                    ListCardDescriptionModel(
                                                        text = it,
                                                    )
                                                },
                                            lastUpdated = item.lastUpdated,
                                            additionalInfoColumnState =
                                                rememberAdditionalInfoColumnState(
                                                    additionalInfoList = item.additionalInfoList,
                                                    syncProgressItem =
                                                        AdditionalInfoItem(
                                                            key = stringResource(id = R.string.syncing),
                                                            value = "",
                                                        ),
                                                    expandLabelText = stringResource(id = R.string.show_more),
                                                    shrinkLabelText = stringResource(id = R.string.show_less),
                                                    scrollableContent = true,
                                                ),
                                        ),
                                    actionButton = {
                                        SyncButtonProvider(state = item.state) {
                                            presenter.onSyncIconClick(item.relatedInfo?.enrollment?.uid)
                                        }
                                    },
                                    onCardClick = {
                                        cardClick(item)
                                    },
                                    listAvatar = {
                                        Avatar(
                                            style =
                                                when (
                                                    val config =
                                                        item.avatarProviderConfiguration
                                                ) {
                                                    is AvatarProviderConfiguration.MainValueLabel ->
                                                        AvatarStyleData.Text(
                                                            config.firstMainValue
                                                                .firstOrNull()
                                                                ?.toString()
                                                                ?: "?",
                                                        )

                                                    is AvatarProviderConfiguration.Metadata ->
                                                        AvatarStyleData.Metadata(
                                                            imageCardData = config.metadataIconData.imageCardData,
                                                            avatarSize = config.size,
                                                            tintColor = config.metadataIconData.color,
                                                        )

                                                    is AvatarProviderConfiguration.ProfilePic ->
                                                        AvatarStyleData.Image(buildPainterForFile(config.profilePicturePath))
                                                },
                                            onImageClick =
                                                when (
                                                    val config =
                                                        item.avatarProviderConfiguration
                                                ) {
                                                    is AvatarProviderConfiguration.Metadata,
                                                    is AvatarProviderConfiguration.MainValueLabel,
                                                    -> null

                                                    is AvatarProviderConfiguration.ProfilePic -> {
                                                        { launchImageDetail(config.profilePicturePath) }
                                                    }
                                                },
                                        )
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }

    private fun cardClick(item: MapItemModel) {
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
    }

    private fun onLocationButtonClicked() {
        teiMapManager?.onLocationButtonClicked(
            locationProvider.hasLocationEnabled(),
            requireActivity(),
        )
    }

    private fun loadMap(
        mapView: MapView,
        savedInstanceState: Bundle?,
    ) {
        teiMapManager =
            TeiMapManager(mapView, MapLocationEngine(requireContext())).also {
                lifecycle.addObserver(it)
                it.onCreate(savedInstanceState)
                it.teiFeatureType = presenter.getTrackedEntityType(tEType).featureType()
                it.enrollmentFeatureType =
                    if (presenter.program != null) presenter.program.featureType() else null
                it.onMapClickListener = OnMapClickListener(it, viewModel::onFeatureClicked)
                it.mapStyle =
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
                it.init(
                    viewModel.fetchMapStyles(),
                    onInitializationFinished = {
                        presenter.getMapData()
                        viewModel.filterVisibleMapItems(
                            it.mapLayerManager.mapLayers.toMap(),
                        )
                    },
                    onMissingPermission = { permissionsManager ->
                        permissionsManager?.requestLocationPermissions(requireActivity())
                    },
                )
            }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        teiMapManager?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        teiMapManager?.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        val exists =
            childFragmentManager
                .findFragmentByTag(MapLayerDialog::class.java.name) as MapLayerDialog?
        exists?.dismiss()
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
}
