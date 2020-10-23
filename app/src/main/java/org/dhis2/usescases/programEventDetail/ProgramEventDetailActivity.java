package org.dhis2.usescases.programEventDetail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityProgramEventDetailBinding;
import org.dhis2.databinding.InfoWindowEventBinding;
import org.dhis2.uicomponents.map.carousel.CarouselAdapter;
import org.dhis2.uicomponents.map.layer.LayerType;
import org.dhis2.uicomponents.map.layer.MapLayerDialog;
import org.dhis2.uicomponents.map.managers.EventMapManager;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.orgunitselector.OUTreeActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.analytics.AnalyticsConstants;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.program.Program;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;
import timber.log.Timber;

import static org.dhis2.R.layout.activity_program_event_detail;
import static org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection.EVENT;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.View,
        MapboxMap.OnMapClickListener {

    private static final String FRAGMENT_TAG = "SYNC";

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.Presenter presenter;

    @Inject
    CarouselViewAnimations animations;

    @Inject
    FiltersAdapter filtersAdapter;

    private ProgramEventDetailLiveAdapter liveAdapter;
    private boolean backDropActive;
    private String programUid;
    private MarkerView currentMarker;
    private FeatureType featureType;
    private EventMapManager eventMapManager;

    public static final String EXTRA_PROGRAM_UID = "PROGRAM_UID";
    private String updateEvent;

    public static Bundle getBundle(String programUid) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PROGRAM_UID, programUid);
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.programUid = getIntent().getStringExtra(EXTRA_PROGRAM_UID);

        ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule(this, programUid)).inject(this);
        super.onCreate(savedInstanceState);

        FilterManager.getInstance().clearCatOptCombo();
        FilterManager.getInstance().clearEventStatus();

        binding = DataBindingUtil.setContentView(this, activity_program_event_detail);

        binding.setPresenter(presenter);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());

        ViewExtensionsKt.clipWithRoundedCorners(binding.recycler, ExtensionsKt.getDp(16));
        liveAdapter = new ProgramEventDetailLiveAdapter(presenter.getProgram(), presenter);
        binding.recycler.setAdapter(liveAdapter);

        filtersAdapter.addEventStatus();
        if (presenter.hasAssignment()) {
            filtersAdapter.addAssignedToMe();
        } else {
            filtersAdapter.removeAssignedToMe();
        }
        try {
            binding.filterLayout.setAdapter(filtersAdapter);

        } catch (Exception e) {
            Timber.e(e);
        }

        binding.mapLayerButton.setOnClickListener(view ->
                new MapLayerDialog(eventMapManager.mapLayerManager)
                        .show(getSupportFragmentManager(), MapLayerDialog.class.getName())
        );

        eventMapManager = new EventMapManager(binding.mapView);
        eventMapManager.setFeatureType(presenter.getFeatureType());
        eventMapManager.setOnMapClickListener(this);
        presenter.init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (eventMapManager != null) {
            eventMapManager.onStart();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isMapVisible()) {
            if (updateEvent != null) {
                animations.initMapLoading(binding.mapCarousel);
                binding.toolbarProgress.show();
                presenter.getEventInfo(updateEvent);
            }
        } else {
            FilterManager.getInstance().publishData();
        }
        if (eventMapManager != null) {
            eventMapManager.onResume();
        }
        binding.addEventButton.setEnabled(true);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        if (eventMapManager != null) {
            eventMapManager.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
        if (eventMapManager != null) {
            eventMapManager.onDestroy();
        }
        binding.mapView.onDestroy();

        FilterManager.getInstance().clearEventStatus();
        FilterManager.getInstance().clearCatOptCombo();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void setProgram(Program program) {
        binding.setName(program.displayName());
    }

    @Override
    public void showFilterProgress() {
        runOnUiThread(() -> {
            if (isMapVisible()) {
                binding.toolbarProgress.setVisibility(View.VISIBLE);
                binding.toolbarProgress.show();
            } else {
                binding.progressLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void setLiveData(LiveData<PagedList<EventViewModel>> pagedListLiveData) {
        pagedListLiveData.observe(this, pagedList -> {
            binding.programProgress.setVisibility(View.GONE);
            liveAdapter.submitList(pagedList, () -> {
                if (binding.recycler.getAdapter() != null && binding.recycler.getAdapter().getItemCount() == 0) {
                    binding.emptyTeis.setVisibility(View.VISIBLE);
                    binding.recycler.setVisibility(View.GONE);
                } else {
                    binding.emptyTeis.setVisibility(View.GONE);
                    binding.recycler.setVisibility(View.VISIBLE);
                }
            });

        });

    }

    @Override
    public void setOptionComboAccess(Boolean canCreateEvent) {
        switch (binding.addEventButton.getVisibility()) {
            case View.VISIBLE:
                binding.addEventButton.setVisibility(canCreateEvent ? View.VISIBLE : View.GONE);
                break;
            case View.GONE:
                binding.addEventButton.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public void renderError(String message) {
        if (getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void showHideFilter() {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        backDropActive = !backDropActive;
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);
        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);

        if (backDropActive) {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.filterLayout, ConstraintSet.BOTTOM, 50);
        } else {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }

        initSet.applyTo(binding.backdropLayout);
    }

    @Override
    public void clearFilters() {
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    public void setFeatureType(FeatureType type) {
        this.featureType = type;
    }

    @Override
    public void startNewEvent() {
        analyticsHelper().setEvent(AnalyticsConstants.CREATE_EVENT, AnalyticsConstants.DATA_CREATION, AnalyticsConstants.CREATE_EVENT);
        binding.addEventButton.setEnabled(false);
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programUid);
        startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        switch (binding.addEventButton.getVisibility()) {
            case View.VISIBLE:
                binding.addEventButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
                break;
            case View.GONE:
                binding.addEventButton.setVisibility(View.GONE);
                break;
        }
        if (binding.addEventButton.getVisibility() == View.VISIBLE) {
            binding.emptyTeis.setText(R.string.empty_tei_add);
        } else {
            binding.emptyTeis.setText(R.string.empty_tei_no_add);
        }
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(2, findViewById(R.id.addEventButton).getVisibility() == View.VISIBLE);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.PROGRAM_EVENT_LIST,
                    stepConditions);

        }, 500);
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos) {
        if (!categoryOptionCombos.val0().isDefault()) {
            filtersAdapter.addCatOptCombFilter(categoryOptionCombos);
        }
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().showFromToSelector(this, FilterManager.getInstance()::addPeriod);
        } else {
            DateUtils.getInstance().showPeriodDialog(this, datePeriods -> {
                        FilterManager.getInstance().addPeriod(datePeriods);
                    },
                    true);
        }
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(this, OUTreeActivity.class);
        Bundle bundle = OUTreeActivity.Companion.getBundle(programUid);
        ouTreeIntent.putExtras(bundle);
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FilterManager.OU_TREE && resultCode == Activity.RESULT_OK) {
            filtersAdapter.notifyDataSetChanged();
            updateFilters(FilterManager.getInstance().getTotalFilters());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }

    @Override
    public void setMap(FeatureCollection featureCollection, BoundingBox boundingBox, List<ProgramEventViewModel> programEventViewModels) {
        eventMapManager.init(() -> {
            eventMapManager.update(
                    featureCollection,
                    boundingBox
            );
            if (binding.mapCarousel.getAdapter() == null) {
                CarouselAdapter carouselAdapter = new CarouselAdapter.Builder()
                        .addOnSyncClickListener(
                                teiUid -> {
                                    if (binding.mapCarousel.getCarouselEnabled()) {
                                        presenter.onSyncIconClick(teiUid);
                                    }
                                    return true;
                                })
                        .addOnEventClickListener((teiUid, orgUnit, eventUid) -> {
                            if (binding.mapCarousel.getCarouselEnabled()) {
                                presenter.onEventClick(teiUid, orgUnit);
                            }
                            return true;
                        })
                        .build();
                binding.mapCarousel.setAdapter(carouselAdapter);
                binding.mapCarousel.attachToMapManager(eventMapManager, () -> true);
                carouselAdapter.addItems(programEventViewModels);
            } else {
                ((CarouselAdapter) binding.mapCarousel.getAdapter()).updateAllData(programEventViewModels);
            }

            eventMapManager.mapLayerManager.selectFeature(null);
            binding.mapLayerButton.setVisibility(View.VISIBLE);
            return Unit.INSTANCE;
        });

        animations.endMapLoading(binding.mapCarousel);
        binding.toolbarProgress.hide();
    }

    @Override
    public void setEventInfo(Pair<ProgramEventViewModel, LatLng> eventInfo) {
        if (currentMarker != null) {
            eventMapManager.getMarkerViewManager().removeMarker(currentMarker);
        }
        InfoWindowEventBinding binding = InfoWindowEventBinding.inflate(LayoutInflater.from(this));
        binding.setEvent(eventInfo.val0());
        binding.setPresenter(presenter);
        View view = binding.getRoot();
        view.setOnClickListener(viewClicked ->
                eventMapManager.getMarkerViewManager().removeMarker(currentMarker));
        view.setOnLongClickListener(view1 -> {
            presenter.onEventClick(eventInfo.val0().uid(), eventInfo.val0().orgUnitUid());
            return true;
        });
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        currentMarker = new MarkerView(eventInfo.val1(), view);
        eventMapManager.getMarkerViewManager().addMarker(currentMarker);
    }

    @Override
    public void updateEventCarouselItem(ProgramEventViewModel programEventViewModel) {
        ((CarouselAdapter) binding.mapCarousel.getAdapter()).updateItem(programEventViewModel);
        animations.endMapLoading(this.binding.mapCarousel);
        this.binding.toolbarProgress.hide();
        updateEvent = null;
    }

    @Override
    public void showMoreOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        popupMenu.getMenuInflater().inflate(R.menu.event_list_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.showHelp:
                    analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                    showTutorial(false);
                    break;
                case R.id.menu_list:
                    showMap(false);
                    break;
                case R.id.menu_map:
                    showMap(true);
                    break;
                default:
                    break;
            }
            return false;
        });
        boolean mapVisible = binding.mapView.getVisibility() != View.GONE;
        boolean listVisible = binding.recycler.getVisibility() != View.GONE;
        boolean emptyVisible = !mapVisible && !listVisible;
        popupMenu.getMenu().getItem(0).setVisible(!emptyVisible && !mapVisible && featureType != FeatureType.NONE);
        popupMenu.getMenu().getItem(1).setVisible(!emptyVisible && binding.recycler.getVisibility() == View.GONE && featureType != FeatureType.NONE);
        popupMenu.show();
    }

    @Override
    public boolean isMapVisible() {
        return binding.mapView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void navigateToEvent(String eventId, String orgUnit) {
        this.updateEvent = eventId;
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programUid);
        bundle.putString(Constants.EVENT_UID, eventId);
        bundle.putString(ORG_UNIT, orgUnit);
        startActivity(EventCaptureActivity.class,
                EventCaptureActivity.getActivityBundle(eventId, programUid, EventMode.CHECK),
                false, false, null
        );
    }

    @Override
    public void showSyncDialog(String uid) {
        SyncStatusDialog dialog = new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.EVENT)
                .setUid(uid)
                .onDismissListener(hasChanged -> {
                    if (hasChanged)
                        FilterManager.getInstance().publishData();

                })
                .build();

        dialog.show(getSupportFragmentManager(), FRAGMENT_TAG);
    }

    private void showMap(boolean showMap) {
        binding.recycler.setVisibility(showMap ? View.GONE : View.VISIBLE);
        binding.mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);
        binding.mapCarousel.setVisibility(showMap ? View.VISIBLE : View.GONE);
        binding.addEventButton.setVisibility(showMap ? View.GONE : View.VISIBLE);

        if (showMap) {
            binding.toolbarProgress.setVisibility(View.VISIBLE);
            binding.toolbarProgress.show();
            presenter.getMapData();
        } else {
            binding.mapLayerButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointf = eventMapManager.getMap().getProjection().toScreenLocation(point);
        RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);
        List<Feature> features = eventMapManager.getMap().queryRenderedFeatures(rectF, featureType == FeatureType.POINT ? "POINT_LAYER" : "POLYGON_LAYER");
        if (!features.isEmpty()) {
            Feature selectedFeature = eventMapManager.findFeature(LayerType.EVENT_LAYER.name(), EVENT, features.get(0).getStringProperty(EVENT));
            eventMapManager.mapLayerManager.getLayer(LayerType.EVENT_LAYER.name(), true).setSelectedItem(selectedFeature);
            binding.mapCarousel.scrollToFeature(features.get(0));
            return true;
        }
        return false;
    }

    @Override
    public void showCatOptComboDialog(String catComboUid) {
        new CategoryDialog(
                CategoryDialog.Type.CATEGORY_OPTION_COMBO,
                catComboUid,
                false,
                null,
                selectedCatOptionCombo -> {
                    presenter.filterCatOptCombo(selectedCatOptionCombo);
                    return null;
                }
        ).show(
                getSupportFragmentManager(),
                CategoryDialog.Companion.getTAG()
        );
    }
}