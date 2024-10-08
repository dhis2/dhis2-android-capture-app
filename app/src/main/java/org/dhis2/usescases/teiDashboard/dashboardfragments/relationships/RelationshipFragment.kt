package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.databinding.DataBindingUtil
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapView
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.bindings.launchImageDetail
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.FragmentRelationshipsBinding
import org.dhis2.form.model.EventMode
import org.dhis2.maps.ExternalMapNavigation
import org.dhis2.maps.camera.centerCameraOnFeatures
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.location.MapLocationEngine
import org.dhis2.maps.managers.RelationshipMapManager
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.dhis2.maps.views.MapScreen
import org.dhis2.maps.views.OnMapClickListener
import org.dhis2.ui.ThemeManager
import org.dhis2.ui.avatar.AvatarProvider
import org.dhis2.ui.theme.Dhis2Theme
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teiDashboard.ui.NoRelationships
import org.dhis2.utils.OnDialogClickListener
import org.dhis2.utils.dialFloatingActionButton.DialItem
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import javax.inject.Inject

class RelationshipFragment : FragmentGlobalAbstract(), RelationshipView {
    @Inject
    lateinit var presenter: RelationshipPresenter

    @Inject
    lateinit var mapNavigation: ExternalMapNavigation

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var colorUtils: ColorUtils

    private lateinit var binding: FragmentRelationshipsBinding
    private lateinit var relationshipAdapter: RelationshipAdapter
    private var relationshipType: RelationshipType? = null
    private var relationshipMapManager: RelationshipMapManager? = null
    private var sources: Set<String>? = null
    private lateinit var mapButtonObservable: MapButtonObservable

    private val addRelationshipLauncher = registerForActivityResult(AddRelationshipContract()) {
        themeManager.setProgramTheme(programUid()!!)
        when (it) {
            is RelationshipResult.Error -> { // Unused
            }

            is RelationshipResult.Success -> {
                presenter.addRelationship(it.teiUidToAddAsRelationship, relationshipType!!.uid())
            }
        }
    }

    private fun programUid(): String? {
        return requireArguments().getString("ARG_PROGRAM_UID")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mapButtonObservable = context as MapButtonObservable
        app().userComponent()?.plus(
            RelationshipModule(
                requireContext(),
                this,
                programUid(),
                requireArguments().getString("ARG_TEI_UID"),
                requireArguments().getString("ARG_ENROLLMENT_UID"),
                requireArguments().getString("ARG_EVENT_UID"),
            ),
        )?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_relationships, container, false)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Dhis2Theme {
                    val showMap by mapButtonObservable.relationshipMap().observeAsState()
                    LaunchedEffect(key1 = showMap) {
                        presenter.takeIf { showMap == true }?.fetchMapData()
                    }
                    when (showMap) {
                        true -> RelationshipMapScreen(savedInstanceState)
                        else -> RelationshipListScreen()
                    }
                }
            }
        }
    }

    @Composable
    private fun RelationshipListScreen() {
        val relationships by presenter.relationshipModels.observeAsState()

        if (relationships?.isEmpty() == true) {
            NoRelationships()
        } else {
            AndroidView(factory = { _ ->
                binding.root
            }, update = {
                binding.relationshipRecycler.adapter =
                    RelationshipAdapter(presenter, colorUtils).also {
                        it.submitList(relationships)
                    }
            })
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

        LaunchedEffect(key1 = items) {
            mapData?.let { data ->
                relationshipMapManager.takeIf { it?.isMapReady() == true }
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
                    this?.findFeatures(item.uid)
                        ?.takeIf { it.isNotEmpty() }?.let { features ->
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
                IconButton(
                    style = IconButtonStyle.TONAL,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_layers),
                            contentDescription = "",
                        )
                    },
                ) {
                    relationshipMapManager?.let {
                        MapLayerDialog(it, programUid()) { layersVisibility ->
                            presenter.filterVisibleMapItems(layersVisibility)
                        }.show(
                            childFragmentManager,
                            MapLayerDialog::class.java.name,
                        )
                    }
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
            onItem = { item ->
                ListCard(
                    modifier = Modifier.fillParentMaxWidth(),
                    listCardState = rememberListCardState(
                        title = ListCardTitleModel(text = item.title),
                        description = item.description?.let {
                            ListCardDescriptionModel(
                                text = it,
                            )
                        },
                        lastUpdated = item.lastUpdated,
                        additionalInfoColumnState = rememberAdditionalInfoColumnState(
                            additionalInfoList = item.additionalInfoList,
                            syncProgressItem = AdditionalInfoItem(
                                key = stringResource(id = R.string.syncing),
                                value = "",
                            ),
                            expandLabelText = stringResource(id = R.string.show_more),
                            shrinkLabelText = stringResource(id = R.string.show_less),
                            scrollableContent = true,
                        ),
                    ),
                    onCardClick = {
                        openDashboardFor(item.uid)
                    },
                    listAvatar = {
                        AvatarProvider(
                            avatarProviderConfiguration = item.avatarProviderConfiguration,
                            onImageClick = ::launchImageDetail,
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

    private fun loadMap(mapView: MapView, savedInstanceState: Bundle?) {
        relationshipMapManager = RelationshipMapManager(mapView, MapLocationEngine(requireContext()))
        relationshipMapManager?.also {
            lifecycle.addObserver(it)
            it.onCreate(savedInstanceState)
            it.onMapClickListener = OnMapClickListener(
                it,
                presenter::onFeatureClicked,
            )
            it.init(
                presenter.fetchMapStyles(),
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
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        relationshipMapManager?.onLowMemory()
    }

    override fun goToAddRelationship(teiUid: String, teiTypeUidToAdd: String) {
        addRelationshipLauncher.launch(
            RelationshipInput(
                teiUid,
                teiTypeUidToAdd,
            ),
        )
    }

    override fun initFab(relationshipTypes: MutableList<Trio<RelationshipType, String, Int>>) {
        val items: MutableList<DialItem> = ArrayList()
        var dialItemIndex = 1
        for (trio in relationshipTypes) {
            val relationshipType = trio.val0()
            val resource = trio.val2()!!
            items.add(
                DialItem(
                    dialItemIndex++,
                    relationshipType!!.displayName()!!,
                    resource,
                ),
            )
        }
        binding.dialFabLayout.addDialItems(items) { clickedId: Int ->
            val selectedRelationShip = relationshipTypes[clickedId - 1]
            goToRelationShip(selectedRelationShip.val0()!!, selectedRelationShip.val1()!!)
        }
    }

    private fun goToRelationShip(relationshipTypeModel: RelationshipType, teiTypeUid: String) {
        relationshipType = relationshipTypeModel
        presenter.goToAddRelationship(teiTypeUid, relationshipType!!)
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

    override fun openEventFor(eventUid: String, programUid: String) {
        val bundle = EventCaptureActivity.getActivityBundle(
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

    override fun setFeatureCollection(
        currentTei: String?,
        relationshipsMapModels: List<RelationshipUiComponentModel>,
        map: Pair<Map<String, FeatureCollection>, BoundingBox>,
    ) {
        relationshipMapManager?.update(map.first, map.second)
        sources = map.first.keys
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
