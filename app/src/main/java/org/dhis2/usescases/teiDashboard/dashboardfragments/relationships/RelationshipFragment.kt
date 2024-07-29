package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.databinding.DataBindingUtil
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity
import org.dhis2.commons.locationprovider.LocationSettingLauncher.requestEnableLocationSetting
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.FragmentRelationshipsBinding
import org.dhis2.form.model.EventMode
import org.dhis2.maps.ExternalMapNavigation
import org.dhis2.maps.camera.centerCameraOnFeatures
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.managers.RelationshipMapManager
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.dhis2.maps.views.MapScreen
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
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
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
    private lateinit var relationshipMapManager: RelationshipMapManager
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
//        relationshipAdapter = RelationshipAdapter(presenter, colorUtils)
//        binding.relationshipRecycler.adapter = relationshipAdapter

//        mapButtonObservable.relationshipMap().observe(viewLifecycleOwner) { showMap ->
//            val mapVisibility = if (showMap) View.VISIBLE else View.GONE
//            val listVisibility = if (showMap) View.GONE else View.VISIBLE
//            binding.relationshipRecycler.visibility = listVisibility
//            binding.mapView.visibility = mapVisibility
//            binding.mapLayerButton.visibility = mapVisibility
//            binding.mapPositionButton.visibility = mapVisibility
//            binding.mapCarousel.visibility = mapVisibility
//            binding.dialFabLayout.setFabVisible(!showMap)
//        }
        /*binding.mapLayerButton.setOnClickListener {
            val layerDialog = MapLayerDialog(
                relationshipMapManager,
            )
            layerDialog.show(childFragmentManager, MapLayerDialog::class.java.name)
        }*/
//        binding.mapPositionButton.setOnClickListener { handleMapPositionClick() }
//        binding.emptyRelationships.setContent { NoRelationships() }
//        return binding.root

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Dhis2Theme {
                    val showMap by mapButtonObservable.relationshipMap().observeAsState()
                    LaunchedEffect(key1 = showMap) {
                        if (showMap == true) {
                            presenter.fetchMapData()
                        }
                    }
                    when (showMap) {
                        true -> {
                            val listState = rememberLazyListState()

                            val mapData by presenter.relationshipMapData.observeAsState()
                            val items by remember {
                                derivedStateOf { mapData?.mapItems ?: emptyList() }
                            }

                            val clickedItem by presenter.mapItemClicked.observeAsState(initial = null)

                            LaunchedEffect(key1 = items) {
                                mapData?.let { data ->
                                    relationshipMapManager.takeIf { it.isMapReady() }
                                        ?.update(data.relationshipFeatures, data.boundingBox)
                                }
                            }

                            LaunchedEffect(key1 = clickedItem) {
                                if (clickedItem != null) {
                                    listState.animateScrollToItem(
                                        items.indexOfFirst { it.uid == clickedItem },
                                    )
                                }
                            }

                            MapScreen(
                                items = items,
                                listState = listState,
                                onItemScrolled = { item ->
                                    with(relationshipMapManager) {
                                        this.requestMapLayerManager()?.selectFeature(null)
                                        this.findFeatures(item.uid)
                                            ?.takeIf { it.isNotEmpty() }?.let { features ->
                                                map?.centerCameraOnFeatures(features)
                                            }
                                    }
                                },
                                onNavigate = { item ->
                                    relationshipMapManager.findFeature(item.uid)?.let { feature ->
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
                                        MapLayerDialog(relationshipMapManager) { layersVisibility ->
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
                                    ) {
                                        handleMapPositionClick()
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
                                        expandLabelText = stringResource(id = R.string.show_more),
                                        shrinkLabelText = stringResource(id = R.string.show_less),
                                        onCardClick = {
                                            openDashboardFor(item.uid)
                                        },
                                    )
                                },
                            )
                        }
                        else -> {
                            val relationships by presenter.relationshipModels.observeAsState()

                            if (relationships?.isEmpty() == true) {
                                NoRelationships()
                            } else {
                                AndroidView(factory = { context ->

                                    binding.root
                                }, update = {
                                    binding.relationshipRecycler.adapter = RelationshipAdapter(presenter, colorUtils).also {
                                        it.submitList(relationships)
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadMap(mapView: MapView, savedInstanceState: Bundle?) {
        relationshipMapManager = RelationshipMapManager(mapView)
        lifecycle.addObserver(relationshipMapManager)
        relationshipMapManager.onCreate(savedInstanceState)
        relationshipMapManager.onMapClickListener =
            org.dhis2.maps.views.OnMapClickListener(relationshipMapManager, presenter::onFeatureClicked)
        relationshipMapManager.init(
            presenter.fetchMapStyles(),
        ) { permissionManager ->
            handleMissingPermission(permissionManager)
        }
    }

    private fun handleMapPositionClick() {
        if (locationProvider.hasLocationEnabled()) {
            relationshipMapManager.centerCameraOnMyPosition { permissionManager ->
                permissionManager?.requestLocationPermissions(
                    activity,
                )
            }
        } else {
            requestEnableLocationSetting(requireContext())
        }
    }

    private fun handleMissingPermission(permissionManager: PermissionsManager?) {
        if (locationProvider.hasLocationEnabled()) {
            permissionManager?.requestLocationPermissions(activity)
        } else {
            requestEnableLocationSetting(requireContext())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        relationshipMapManager.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (
            mapButtonObservable.relationshipMap().value == true &&
            relationshipMapManager.permissionsManager != null
        ) {
            relationshipMapManager.permissionsManager?.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (mapButtonObservable.relationshipMap().value == true) {
//            animations.initMapLoading(binding.mapCarousel)
        }
        presenter.init()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        relationshipMapManager.onLowMemory()
    }

    override fun setRelationships(relationships: List<RelationshipViewModel>) {
        /*relationshipAdapter.submitList(relationships)
        if (relationships.isNotEmpty()) {
            binding.emptyRelationships.visibility = View.GONE
        } else {
            binding.emptyRelationships.visibility = View.VISIBLE
        }*/
    }

    override fun goToAddRelationship(teiUid: String, teiTypeUidToAdd: String) {
        if (activity is TeiDashboardMobileActivity) {
            (activity as TeiDashboardMobileActivity?)?.toRelationships()
        }
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
        relationshipMapManager.update(map.first, map.second)
        sources = map.first.keys
//        val carouselAdapter = CarouselAdapter.Builder()
//            .addCurrentTei(currentTei)
//            .addOnDeleteRelationshipListener { relationshipUid ->
//                if (binding.mapCarousel.carouselEnabled) {
//                    presenter.deleteRelationship(relationshipUid)
//                }
//                true
//            }
//            .addOnRelationshipClickListener { teiUid, ownerType ->
//                if (binding.mapCarousel.carouselEnabled) {
//                    presenter.onRelationshipClicked(ownerType, teiUid)
//                }
//                true
//            }
//            /*.addOnNavigateClickListener { uid ->
//                val feature = relationshipMapManager.findFeature(uid)
//                if (feature != null) {
//                    startActivity(mapNavigation.navigateToMapIntent(feature))
//                }
//            }*/
//            .build()
// //        binding.mapCarousel.setAdapter(carouselAdapter)
// //        binding.mapCarousel.attachToMapManager(relationshipMapManager)
//        carouselAdapter.addItems(relationshipsMapModels)
// //        animations.endMapLoading(binding.mapCarousel)
//        mapButtonObservable.onRelationshipMapLoaded()
    }

   /* override fun onMapClick(point: LatLng): Boolean {
        val pointf = relationshipMapManager.map!!.projection.toScreenLocation(point)
        val rectF = RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)
        sources?.forEach { sourceId ->
            val lineLayerId = "RELATIONSHIP_LINE_LAYER_ID_$sourceId"
            val pointLayerId = "RELATIONSHIP_LINE_LAYER_ID_$sourceId"
            val features = relationshipMapManager.map
                ?.queryRenderedFeatures(rectF, lineLayerId, pointLayerId)
            if (features?.isNotEmpty() == true) {
                relationshipMapManager.mapLayerManager.selectFeature(null)
                val selectedFeature = relationshipMapManager.findFeature(
                    sourceId,
                    MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
                    features[0].getStringProperty(
                        MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
                    ),
                )
                relationshipMapManager.mapLayerManager.getLayer(sourceId, true)
                    ?.setSelectedItem(selectedFeature)
//                binding.mapCarousel.scrollToFeature(features[0])
                return true
            }
        }
        return false
    }*/

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
