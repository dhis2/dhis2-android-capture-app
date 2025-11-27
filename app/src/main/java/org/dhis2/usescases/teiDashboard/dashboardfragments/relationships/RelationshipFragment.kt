package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.bindings.launchImageDetail
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.model.EventMode
import org.dhis2.maps.ExternalMapNavigation
import org.dhis2.maps.camera.centerCameraOnFeatures
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.location.MapLocationEngine
import org.dhis2.maps.managers.RelationshipMapManager
import org.dhis2.maps.views.LocationIcon
import org.dhis2.maps.views.MapScreen
import org.dhis2.maps.views.OnMapClickListener
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration
import org.dhis2.tracker.relationships.ui.DeleteRelationshipsConfirmation
import org.dhis2.tracker.relationships.ui.RelationShipsScreen
import org.dhis2.tracker.relationships.ui.RelationshipsViewModel
import org.dhis2.tracker.relationships.ui.state.RelationshipSectionUiState
import org.dhis2.tracker.relationships.ui.state.RelationshipTopBarIconState
import org.dhis2.tracker.relationships.ui.state.RelationshipsUiState
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.OnDialogClickListener
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
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapView
import javax.inject.Inject

class RelationshipFragment :
    FragmentGlobalAbstract(),
    RelationshipView {
    @Inject
    lateinit var presenter: RelationshipPresenter

    @Inject
    lateinit var mapNavigation: ExternalMapNavigation

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var colorUtils: ColorUtils

    @Inject
    lateinit var relationShipsViewModel: RelationshipsViewModel

    private var relationshipSection: RelationshipSectionUiState? = null
    private var relationshipMapManager: RelationshipMapManager? = null
    private lateinit var mapButtonObservable: MapButtonObservable

    private val addRelationshipLauncher =
        registerForActivityResult(AddRelationshipContract()) {
            themeManager.setProgramTheme(programUid()!!)
            when (it) {
                is RelationshipResult.Error -> { // Unused
                }

                is RelationshipResult.Success -> {
                    relationshipSection?.let { relationshipSection ->
                        relationShipsViewModel.onAddRelationship(
                            selectedTeiUid = it.teiUidToAddAsRelationship,
                            relationshipTypeUid = relationshipSection.uid,
                            relationshipSide = relationshipSection.side,
                        )
                    }
                }
            }
        }

    private fun programUid(): String? = requireArguments().getString("ARG_PROGRAM_UID")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MapButtonObservable) {
            mapButtonObservable = context
            app()
                .userComponent()
                ?.plus(
                    RelationshipModule(
                        requireContext(),
                        this,
                        programUid(),
                        requireArguments().getString("ARG_TEI_UID"),
                        requireArguments().getString("ARG_ENROLLMENT_UID"),
                        requireArguments().getString("ARG_EVENT_UID"),
                    ),
                )?.inject(this)
        } else {
            throw ClassCastException("$context must implement MapButtonObservable")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DHIS2Theme {
                    val showMap by mapButtonObservable.relationshipMap().observeAsState()

                    val uiState by relationShipsViewModel.relationshipsUiState.collectAsState()
                    val relationshipSelectionState by relationShipsViewModel.relationshipSelectionState.collectAsState()
                    val showDeleteConfirmation by relationShipsViewModel.showDeleteConfirmation.collectAsState()

                    when (showMap) {
                        true -> RelationshipMapScreen(savedInstanceState)
                        else ->
                            RelationShipsScreen(
                                uiState = uiState,
                                relationshipSelectionState = relationshipSelectionState,
                                onCreateRelationshipClick = {
                                    relationshipSection = it
                                    presenter.goToAddRelationship(
                                        it.uid,
                                        it.entityToAdd,
                                    )
                                },
                                onRelationshipClick = {
                                    presenter.onRelationshipClicked(
                                        ownerType = it.ownerType,
                                        ownerUid = it.ownerUid,
                                    )
                                },
                                onRelationShipSelected = relationShipsViewModel::updateSelectedList,
                            )
                    }

                    if (showDeleteConfirmation) {
                        (uiState as? RelationshipsUiState.Success)?.let { state ->
                            DeleteRelationshipsConfirmation(
                                relationships =
                                    relationshipSelectionState.selectedItems.map { selectedUid ->
                                        state.data
                                            .first {
                                                it.relationships.any { it.uid == selectedUid }
                                            }.title
                                    },
                                onDelete = {
                                    relationShipsViewModel.deleteSelectedRelationships()
                                },
                                onDismiss = {
                                    relationShipsViewModel.onDismissDelete()
                                },
                            )
                        }
                    }
                }
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeRelationshipTopBarIcon()
    }

    private fun observeRelationshipTopBarIcon() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                relationShipsViewModel.relationshipSelectionState.collect { selectionState ->
                    val topBarIconState =
                        if (selectionState.selectingMode) {
                            RelationshipTopBarIconState.Selecting {
                                relationShipsViewModel.onDeleteClick()
                            }
                        } else {
                            RelationshipTopBarIconState.List()
                        }
                    mapButtonObservable.updateRelationshipsTopBarIconState(topBarIconState)
                }
            }
        }
    }

    @Composable
    private fun RelationshipMapScreen(savedInstanceState: Bundle?) {
        val listState = rememberLazyListState()

        val mapData by presenter.relationshipMapData.observeAsState()
        val items by remember {
            derivedStateOf { mapData?.mapItems ?: emptyList() }
        }

        val clickedItem by presenter.mapItemClicked.observeAsState(initial = null)
        val locationState = relationshipMapManager?.locationState?.collectAsState()
        val mapDataFinishedLoading = relationshipMapManager?.dataFinishedLoading?.collectAsState()

        LaunchedEffect(key1 = items) {
            mapData?.let { data ->
                relationshipMapManager
                    .takeIf { it?.isMapReady() == true }
                    ?.update(data.relationshipFeatures, data.boundingBox)
            }
        }

        LaunchedEffect(key1 = clickedItem) {
            listState.takeIf { clickedItem != null }?.animateScrollToItem(
                items.indexOfFirst { it.uid == clickedItem },
            )
        }

        MapScreen(
            items = items,
            listState = listState,
            onItemScrolled = { item ->
                with(relationshipMapManager) {
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
                relationshipMapManager?.findFeature(item.uid)?.let { feature ->
                    startActivity(mapNavigation.navigateToMapIntent(feature))
                }
            },
            map = {
                AndroidView(factory = { context ->
                    val map = MapView(context)
                    loadMap(map, savedInstanceState)
                    map
                }) {
                }
            },
            actionButtons = {
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
                            relationshipMapManager?.let {
                                val dialog = MapLayerDialog.newInstance(programUid())
                                dialog.mapManager = it
                                dialog
                                    .setOnLayersVisibilityListener { layersVisibility ->
                                        presenter.filterVisibleMapItems(layersVisibility)
                                    }.show(childFragmentManager, MapLayerDialog::class.java.name)
                            }
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
            onItem = { item ->
                ListCard(
                    modifier = Modifier.fillParentMaxWidth(),
                    listCardState =
                        rememberListCardState(
                            title = ListCardTitleModel(text = item.title),
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
                    onCardClick = {
                        presenter.onMapRelationshipClicked(item.uid)
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
                                            config.firstMainValue.firstOrNull()?.toString()
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

    private fun onLocationButtonClicked() {
        relationshipMapManager?.onLocationButtonClicked(
            locationProvider.hasLocationEnabled(),
            requireActivity(),
        )
    }

    private fun loadMap(
        mapView: MapView,
        savedInstanceState: Bundle?,
    ) {
        relationshipMapManager =
            RelationshipMapManager(mapView, MapLocationEngine(requireContext()))
        relationshipMapManager?.also {
            lifecycle.addObserver(it)
            it.onCreate(savedInstanceState)
            it.onMapClickListener =
                OnMapClickListener(
                    it,
                    presenter::onFeatureClicked,
                )
            it.init(
                presenter.fetchMapStyles(),
                onInitializationFinished = {
                    presenter.relationshipMapData.value?.let { data ->
                        relationshipMapManager?.update(data.relationshipFeatures, data.boundingBox)
                    }
                },
            ) { permissionManager ->
                handleMissingPermission(permissionManager)
            }
        }
    }

    private fun handleMissingPermission(permissionManager: PermissionsManager?) {
        if (locationProvider.hasLocationEnabled()) {
            permissionManager?.requestLocationPermissions(activity)
        } else {
            LocationSettingLauncher.requestEnableLocationSetting(requireContext())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        relationshipMapManager?.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (
            mapButtonObservable.relationshipMap().value == true &&
            relationshipMapManager?.permissionsManager != null
        ) {
            relationshipMapManager?.permissionsManager?.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
        relationShipsViewModel.refreshRelationships()
        val exists =
            childFragmentManager
                .findFragmentByTag(MapLayerDialog::class.java.name) as MapLayerDialog?
        exists?.dismiss()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        relationshipMapManager?.onLowMemory()
    }

    override fun goToAddRelationship(
        teiUid: String,
        teiTypeUidToAdd: String,
    ) {
        addRelationshipLauncher.launch(
            RelationshipInput(
                teiUid,
                teiTypeUidToAdd,
            ),
        )
    }

    override fun showPermissionError() {
        displayMessage(getString(R.string.search_access_error))
    }

    override fun openDashboardFor(teiUid: String) {
        requireActivity().startActivity(
            TeiDashboardMobileActivity.intent(
                context,
                teiUid,
                null,
                null,
            ),
        )
    }

    override fun openEventFor(
        eventUid: String,
        programUid: String,
    ) {
        val bundle =
            EventCaptureActivity.getActivityBundle(
                eventUid,
                programUid,
                EventMode.CHECK,
            )
        val intent = Intent(context, EventCaptureActivity::class.java)
        intent.putExtras(bundle)
        requireActivity().startActivity(intent)
    }

    override fun showTeiWithoutEnrollmentError(teiTypeName: String) {
        showInfoDialog(
            String.format(
                getString(R.string.resource_not_found),
                teiTypeName,
            ),
            getString(R.string.relationship_without_enrollment),
            getString(R.string.button_ok),
            getString(R.string.no),
            object : OnDialogClickListener {
                override fun onPositiveClick() { // Unused
                }

                override fun onNegativeClick() { // Unused
                }
            },
        )
    }

    override fun showRelationshipNotFoundError(teiTypeName: String) {
        showInfoDialog(
            String.format(
                getString(R.string.resource_not_found),
                teiTypeName,
            ),
            getString(R.string.relationship_not_found_message),
            getString(R.string.button_ok),
            getString(R.string.no),
            object : OnDialogClickListener {
                override fun onPositiveClick() { // Unused
                }

                override fun onNegativeClick() { // Unused
                }
            },
        )
    }

    companion object {
        const val TEI_A_UID = "TEI_A_UID"

        @JvmStatic
        fun withArguments(
            programUid: String?,
            teiUid: String?,
            enrollmentUid: String?,
            eventUid: String?,
        ): Bundle {
            val bundle = Bundle()
            bundle.putString("ARG_PROGRAM_UID", programUid)
            bundle.putString("ARG_TEI_UID", teiUid)
            bundle.putString("ARG_ENROLLMENT_UID", enrollmentUid)
            bundle.putString("ARG_EVENT_UID", eventUid)
            return bundle
        }
    }
}
