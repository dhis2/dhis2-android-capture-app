package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.commons.locationprovider.LocationSettingLauncher;
import org.dhis2.maps.ExternalMapNavigation;
import org.dhis2.maps.carousel.CarouselAdapter;
import org.dhis2.maps.layer.MapLayerDialog;
import org.dhis2.maps.managers.RelationshipMapManager;
import org.dhis2.maps.model.RelationshipUiComponentModel;
import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.commons.data.RelationshipViewModel;
import org.dhis2.commons.data.tuples.Trio;
import org.dhis2.databinding.FragmentRelationshipsBinding;
import org.dhis2.ui.ThemeManager;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.commons.Constants;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.dialFloatingActionButton.DialItem;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import kotlin.Unit;

import static android.app.Activity.RESULT_OK;

import static org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.RELATIONSHIP_UID;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public class RelationshipFragment extends FragmentGlobalAbstract implements RelationshipView, MapboxMap.OnMapClickListener {

    @Inject
    RelationshipPresenter presenter;
    @Inject
    CarouselViewAnimations animations;
    @Inject
    ExternalMapNavigation mapNavigation;
    @Inject
    ThemeManager themeManager;

    private FragmentRelationshipsBinding binding;

    private RelationshipAdapter relationshipAdapter;
    private RelationshipType relationshipType;
    private RelationshipMapManager relationshipMapManager;

    public static final String TEI_A_UID = "TEI_A_UID";
    private Set<String> sources;
    private MapButtonObservable mapButtonObservable;

    public static Bundle withArguments(
            String programUid,
            String teiUid,
            String enrollmentUid,
            String eventUid
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("ARG_PROGRAM_UID", programUid);
        bundle.putString("ARG_TEI_UID", teiUid);
        bundle.putString("ARG_ENROLLMENT_UID", enrollmentUid);
        bundle.putString("ARG_EVENT_UID", eventUid);
        return bundle;
    }

    private String programUid() {
        return getArguments().getString("ARG_PROGRAM_UID");
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mapButtonObservable = (MapButtonObservable) context;
        if (((App) context.getApplicationContext()).userComponent() != null)
            ((App) context.getApplicationContext()).userComponent()
                    .plus(new RelationshipModule(
                            this,
                            programUid(),
                            getArguments().getString("ARG_TEI_UID"),
                            getArguments().getString("ARG_ENROLLMENT_UID"),
                            getArguments().getString("ARG_EVENT_UID"))
                    ).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_relationships, container, false);
        relationshipAdapter = new RelationshipAdapter(presenter);
        binding.relationshipRecycler.setAdapter(relationshipAdapter);
        relationshipMapManager = new RelationshipMapManager(binding.mapView);
        getLifecycle().addObserver(relationshipMapManager);
        relationshipMapManager.onCreate(savedInstanceState);
        relationshipMapManager.setOnMapClickListener(this);
        relationshipMapManager.init(() -> Unit.INSTANCE, (permissionManager) -> {
            if (locationProvider.hasLocationEnabled()) {
                permissionManager.requestLocationPermissions(getActivity());
            } else {
                LocationSettingLauncher.INSTANCE.requestEnableLocationSetting(requireContext(), null, () -> null);
            }
            return Unit.INSTANCE;
        });

        mapButtonObservable.relationshipMap().observe(getViewLifecycleOwner(), showMap -> {
            binding.relationshipRecycler.setVisibility(showMap ? View.GONE : View.VISIBLE);
            binding.mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);
            binding.mapLayerButton.setVisibility(showMap ? View.VISIBLE : View.GONE);
            binding.mapPositionButton.setVisibility(showMap ? View.VISIBLE : View.GONE);
            binding.mapCarousel.setVisibility(showMap ? View.VISIBLE : View.GONE);
            binding.dialFabLayout.setFabVisible(!showMap);
        });

        binding.mapLayerButton.setOnClickListener(view -> {
            MapLayerDialog layerDialog = new MapLayerDialog(relationshipMapManager);
            layerDialog.show(getChildFragmentManager(), MapLayerDialog.class.getName());
        });

        binding.mapPositionButton.setOnClickListener(view -> {
            if (locationProvider.hasLocationEnabled()) {
                relationshipMapManager.centerCameraOnMyPosition((permissionManager) -> {
                    permissionManager.requestLocationPermissions(getActivity());
                    return Unit.INSTANCE;
                });
            } else {
                LocationSettingLauncher.INSTANCE.requestEnableLocationSetting(requireContext(), null, () -> null);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        relationshipMapManager.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (binding.mapView.getVisibility() == View.VISIBLE && relationshipMapManager.getPermissionsManager() != null) {
            relationshipMapManager.getPermissionsManager().onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding.mapView.getVisibility() == View.VISIBLE) {
            animations.initMapLoading(binding.mapCarousel);
        }
        presenter.init();
    }

    @Override
    public void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        relationshipMapManager.onLowMemory();
    }

    @Override
    public void setRelationships(List<RelationshipViewModel> relationships) {
        if (relationshipAdapter != null) {
            relationshipAdapter.submitList(relationships);
        }
        if (relationships != null && !relationships.isEmpty()) {
            binding.emptyRelationships.setVisibility(View.GONE);
        } else {
            binding.emptyRelationships.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void goToAddRelationship(@NotNull String teiUid, @NotNull String teiTypeToAdd) {

        Intent intent = new Intent(getContext(), SearchTEActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean("FROM_RELATIONSHIP", true);
        extras.putString("FROM_RELATIONSHIP_TEI", teiUid);
        extras.putString("TRACKED_ENTITY_UID", teiTypeToAdd);
        extras.putString("PROGRAM_UID", null);
        intent.putExtras(extras);

        if (getActivity() instanceof TeiDashboardMobileActivity) {
            ((TeiDashboardMobileActivity) getActivity()).toRelationships();
        }
        this.startActivityForResult(intent, Constants.REQ_ADD_RELATIONSHIP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        themeManager.setProgramTheme(programUid());
        if (requestCode == Constants.REQ_ADD_RELATIONSHIP) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String tei_a = data.getStringExtra(TEI_A_UID);
                    presenter.addRelationship(tei_a, relationshipType.uid());
                }
            }
        }
    }

    @Override
    public void initFab(List<Trio<RelationshipType, String, Integer>> relationshipTypes) {
        List<DialItem> items = new ArrayList<>();
        int dialItemIndex = 1;
        for (Trio<RelationshipType, String, Integer> trio : relationshipTypes) {
            RelationshipType relationshipType = trio.val0();
            int resource = trio.val2();
            items.add(
                    new DialItem(
                            dialItemIndex++,
                            relationshipType.displayName(),
                            resource)
            );
        }

        binding.dialFabLayout.addDialItems(items, clickedId -> {
            Trio<RelationshipType, String, Integer> selectedRelationShip = relationshipTypes.get(clickedId - 1);
            goToRelationShip(selectedRelationShip.val0(), selectedRelationShip.val1());
            return Unit.INSTANCE;
        });

    }

    private void goToRelationShip(@NonNull RelationshipType relationshipTypeModel,
                                  @NonNull String teiTypeUid) {
        relationshipType = relationshipTypeModel;
        presenter.goToAddRelationship(teiTypeUid, relationshipType);
    }

    @Override
    public void showPermissionError() {
        displayMessage(getString(R.string.search_access_error));
    }

    @Override
    public void openDashboardFor(@NotNull String teiUid) {
        getActivity().startActivity(TeiDashboardMobileActivity.intent(getContext(), teiUid, null, null));
    }

    @Override
    public void openEventFor(@NonNull String eventUid, @NonNull String programUid) {
        // TODO: remove empty strings
        Bundle bundle = EventCaptureActivity.getActivityBundle(
                eventUid,
                programUid,
                EventMode.CHECK, "", ""
        );
        Intent intent = new Intent(getContext(), EventCaptureActivity.class);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    @Override
    public void showTeiWithoutEnrollmentError(@NotNull String teiTypeName) {
        showInfoDialog(
                String.format(
                        getString(R.string.resource_not_found),
                        teiTypeName),
                getString(R.string.relationship_without_enrollment),
                getString(R.string.button_ok),
                getString(R.string.no),
                new OnDialogClickListener() {
                    @Override
                    public void onPositiveClick() {

                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
    }

    @Override
    public void showRelationshipNotFoundError(@NotNull String teiTypeName) {
        showInfoDialog(
                String.format(
                        getString(R.string.resource_not_found),
                        teiTypeName),
                getString(R.string.relationship_not_found_message),
                getString(R.string.button_ok),
                getString(R.string.no),
                new OnDialogClickListener() {
                    @Override
                    public void onPositiveClick() {

                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
    }

    @Override
    public void setFeatureCollection(
            String currentTei,
            @NonNull List<RelationshipUiComponentModel> relationships,
            @NotNull kotlin.Pair<? extends Map<String, FeatureCollection>, ? extends BoundingBox> map) {
        relationshipMapManager.update(
                map.getFirst(),
                map.getSecond()
        );
        this.sources = map.getFirst().keySet();

        CarouselAdapter carouselAdapter =
                new CarouselAdapter.Builder()
                        .addCurrentTei(currentTei)
                        .addOnDeleteRelationshipListener(relationshipUid -> {
                            if (binding.mapCarousel.getCarouselEnabled()) {
                                presenter.deleteRelationship(relationshipUid);
                            }
                            return true;
                        })
                        .addOnRelationshipClickListener((teiUid, ownerType) -> {
                            if (binding.mapCarousel.getCarouselEnabled()) {
                                presenter.onRelationshipClicked(ownerType, teiUid);
                            }
                            return true;
                        })
                        .addOnNavigateClickListener(uid -> {
                            Feature feature = relationshipMapManager.findFeature(uid);
                            if (feature != null) {
                                startActivity(mapNavigation.navigateToMapIntent(feature));
                            }
                            return Unit.INSTANCE;
                        })
                        .build();
        binding.mapCarousel.setAdapter(carouselAdapter);
        binding.mapCarousel.attachToMapManager(relationshipMapManager);
        carouselAdapter.addItems(relationships);

        animations.endMapLoading(binding.mapCarousel);
        mapButtonObservable.onRelationshipMapLoaded();
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointf = relationshipMapManager.getMap().getProjection().toScreenLocation(point);
        RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);

        for (String sourceId : sources) {
            String lineLayerId = "RELATIONSHIP_LINE_LAYER_ID_" + sourceId;
            String pointLayerId = "RELATIONSHIP_LINE_LAYER_ID_" + sourceId;

            List<Feature> features = relationshipMapManager.getMap()
                    .queryRenderedFeatures(rectF, lineLayerId, pointLayerId);
            if (!features.isEmpty()) {
                relationshipMapManager.mapLayerManager.selectFeature(null);
                Feature selectedFeature = relationshipMapManager.findFeature(sourceId, RELATIONSHIP_UID, features.get(0).getStringProperty(RELATIONSHIP_UID));
                relationshipMapManager.mapLayerManager.getLayer(sourceId, true).setSelectedItem(selectedFeature);
                binding.mapCarousel.scrollToFeature(features.get(0));
                return true;
            }
        }

        return false;
    }
}
