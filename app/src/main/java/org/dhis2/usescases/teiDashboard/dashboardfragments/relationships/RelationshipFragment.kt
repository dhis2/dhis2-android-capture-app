package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.animations.CarouselViewAnimations
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.locationprovider.LocationSettingLauncher.requestEnableLocationSetting
import org.dhis2.databinding.FragmentRelationshipsBinding
import org.dhis2.maps.ExternalMapNavigation
import org.dhis2.maps.carousel.CarouselAdapter
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.layer.MapLayerDialog
import org.dhis2.maps.managers.RelationshipMapManager
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.EventMode
import org.dhis2.utils.OnDialogClickListener
import org.dhis2.utils.dialFloatingActionButton.DialItem
import org.hisp.dhis.android.core.relationship.RelationshipType

class RelationshipFragment : FragmentGlobalAbstract(), RelationshipView, OnMapClickListener {
    @Inject
    lateinit var presenter: RelationshipPresenter

    @Inject
    lateinit var animations: CarouselViewAnimations

    @Inject
    lateinit var mapNavigation: ExternalMapNavigation

    @Inject
    lateinit var themeManager: ThemeManager

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
                this,
                programUid(),
                requireArguments().getString("ARG_TEI_UID"),
                requireArguments().getString("ARG_ENROLLMENT_UID"),
                requireArguments().getString("ARG_EVENT_UID")
            )
        )?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_relationships, container, false)
        relationshipAdapter = RelationshipAdapter(presenter)
        binding.relationshipRecycler.adapter = relationshipAdapter
        relationshipMapManager = RelationshipMapManager(binding.mapView)
        lifecycle.addObserver(relationshipMapManager)
        relationshipMapManager.onCreate(savedInstanceState)
        relationshipMapManager.onMapClickListener = this
        relationshipMapManager.init(
            presenter.fetchMapStyles()
        ) { permissionManager ->
            handleMissingPermission(permissionManager)
        }
        mapButtonObservable.relationshipMap().observe(viewLifecycleOwner) { showMap ->
            val mapVisibility = if (showMap) View.VISIBLE else View.GONE
            val listVisibility = if (showMap) View.GONE else View.VISIBLE
            binding.relationshipRecycler.visibility = listVisibility
            binding.mapView.visibility = mapVisibility
            binding.mapLayerButton.visibility = mapVisibility
            binding.mapPositionButton.visibility = mapVisibility
            binding.mapCarousel.visibility = mapVisibility
            binding.dialFabLayout.setFabVisible(!showMap)
        }
        binding.mapLayerButton.setOnClickListener {
            val layerDialog = MapLayerDialog(
                relationshipMapManager
            )
            layerDialog.show(childFragmentManager, MapLayerDialog::class.java.name)
        }
        binding.mapPositionButton.setOnClickListener { handleMapPositionClick() }
        return binding.root
    }

    private fun handleMapPositionClick() {
        if (locationProvider.hasLocationEnabled()) {
            relationshipMapManager.centerCameraOnMyPosition { permissionManager ->
                permissionManager?.requestLocationPermissions(
                    activity
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (
            binding.mapView.visibility == View.VISIBLE &&
            relationshipMapManager.permissionsManager != null
        ) {
            relationshipMapManager.permissionsManager?.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.mapView.visibility == View.VISIBLE) {
            animations.initMapLoading(binding.mapCarousel)
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
        relationshipAdapter.submitList(relationships)
        if (relationships.isNotEmpty()) {
            binding.emptyRelationships.visibility = View.GONE
        } else {
            binding.emptyRelationships.visibility = View.VISIBLE
        }
    }

    override fun goToAddRelationship(teiUid: String, teiTypeUidToAdd: String) {
        if (activity is TeiDashboardMobileActivity) {
            (activity as TeiDashboardMobileActivity?)?.toRelationships()
        }
        addRelationshipLauncher.launch(
            RelationshipInput(
                teiUid,
                teiTypeUidToAdd
            )
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
                    resource
                )
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
                null
            )
        )
    }

    override fun openEventFor(eventUid: String, programUid: String) {
        val bundle = EventCaptureActivity.getActivityBundle(
            eventUid,
            programUid,
            EventMode.CHECK
        )
        val intent = Intent(context, EventCaptureActivity::class.java)
        intent.putExtras(bundle)
        requireActivity().startActivity(intent)
    }

    override fun showTeiWithoutEnrollmentError(teiTypeName: String) {
        showInfoDialog(
            String.format(
                getString(R.string.resource_not_found),
                teiTypeName
            ),
            getString(R.string.relationship_without_enrollment),
            getString(R.string.button_ok),
            getString(R.string.no),
            object : OnDialogClickListener {
                override fun onPositiveClick() { // Unused
                }

                override fun onNegativeClick() { // Unused
                }
            }
        )
    }

    override fun showRelationshipNotFoundError(teiTypeName: String) {
        showInfoDialog(
            String.format(
                getString(R.string.resource_not_found),
                teiTypeName
            ),
            getString(R.string.relationship_not_found_message),
            getString(R.string.button_ok),
            getString(R.string.no),
            object : OnDialogClickListener {
                override fun onPositiveClick() { // Unused
                }

                override fun onNegativeClick() { // Unused
                }
            }
        )
    }

    override fun setFeatureCollection(
        currentTei: String?,
        relationshipsMapModels: List<RelationshipUiComponentModel>,
        map: Pair<Map<String, FeatureCollection>, BoundingBox>
    ) {
        relationshipMapManager.update(map.first, map.second)
        sources = map.first.keys
        val carouselAdapter = CarouselAdapter.Builder()
            .addCurrentTei(currentTei)
            .addOnDeleteRelationshipListener { relationshipUid ->
                if (binding.mapCarousel.carouselEnabled) {
                    presenter.deleteRelationship(relationshipUid)
                }
                true
            }
            .addOnRelationshipClickListener { teiUid, ownerType ->
                if (binding.mapCarousel.carouselEnabled) {
                    presenter.onRelationshipClicked(ownerType, teiUid)
                }
                true
            }
            .addOnNavigateClickListener { uid ->
                val feature = relationshipMapManager.findFeature(uid)
                if (feature != null) {
                    startActivity(mapNavigation.navigateToMapIntent(feature))
                }
            }
            .build()
        binding.mapCarousel.setAdapter(carouselAdapter)
        binding.mapCarousel.attachToMapManager(relationshipMapManager)
        carouselAdapter.addItems(relationshipsMapModels)
        animations.endMapLoading(binding.mapCarousel)
        mapButtonObservable.onRelationshipMapLoaded()
    }

    override fun onMapClick(point: LatLng): Boolean {
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
                        MapRelationshipsToFeatureCollection.RELATIONSHIP_UID
                    )
                )
                relationshipMapManager.mapLayerManager.getLayer(sourceId, true)
                    ?.setSelectedItem(selectedFeature)
                binding.mapCarousel.scrollToFeature(features[0])
                return true
            }
        }
        return false
    }

    companion object {
        const val TEI_A_UID = "TEI_A_UID"

        @JvmStatic
        fun withArguments(
            programUid: String?,
            teiUid: String?,
            enrollmentUid: String?,
            eventUid: String?
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
