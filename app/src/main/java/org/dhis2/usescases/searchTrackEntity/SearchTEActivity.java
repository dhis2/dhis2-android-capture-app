package org.dhis2.usescases.searchTrackEntity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.data.forms.dataentry.ProgramAdapter;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ActivitySearchBinding;
import org.dhis2.uicomponents.map.carousel.CarouselAdapter;
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage;
import org.dhis2.uicomponents.map.layer.MapLayerDialog;
import org.dhis2.uicomponents.map.managers.TeiMapManager;
import org.dhis2.uicomponents.map.mapper.MapRelationshipToRelationshipMapModel;
import org.dhis2.uicomponents.map.model.CarouselItemModel;
import org.dhis2.uicomponents.map.model.EventUiComponentModel;
import org.dhis2.uicomponents.map.model.MapStyle;
import org.dhis2.usescases.coodinates.CoordinatesView;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.orgunitselector.OUTreeActivity;
import org.dhis2.usescases.searchTrackEntity.adapters.FormAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.RelationshipLiveAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.customviews.ImageDetailBottomDialog;
import org.dhis2.utils.customviews.ScanTextView;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.Filters;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.idlingresource.CountingIdlingResourceSingleton;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import kotlin.Pair;
import kotlin.Unit;
import timber.log.Timber;

import static org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.RELATIONSHIP_UID;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST;
import static org.dhis2.utils.analytics.AnalyticsConstants.CHANGE_PROGRAM;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

@BindingMethods({
        @BindingMethod(type = FloatingActionButton.class, attribute = "app:srcCompat", method = "setImageDrawable")
})
public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View,
        MapboxMap.OnMapClickListener {

    ActivitySearchBinding binding;
    @Inject
    SearchTEContractsModule.Presenter presenter;
    @Inject
    CarouselViewAnimations animations;
    @Inject
    FiltersAdapter filtersAdapter;

    private String initialProgram;
    private String tEType;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;
    private boolean backDropActive;
    /**
     * 0 - it is general filter
     * 1 - it is search filter
     * 2 - it was closed
     */
    private int switchOpenClose = 2;

    ObservableBoolean needsSearch = new ObservableBoolean(true);

    private SearchTeiLiveAdapter liveAdapter;
    private RelationshipLiveAdapter relationshipLiveAdapter;
    private FeatureType featureType;
    private TeiMapManager teiMapManager;
    private boolean initSearchNeeded = true;
    private Snackbar downloadingSnackbar;
    private String currentStyle = Style.MAPBOX_STREETS;
    private ObjectAnimator animation = null;
    private Set<String> sources;
    private Set<String> eventSources;
    private String updateTei;
    private String updateEvent;
    private CarouselAdapter carouselAdapter;

    //---------------------------------------------------------------------------------------------

    //region LIFECYCLE
    @Override
    protected void onStart() {
        super.onStart();
        if (teiMapManager != null) {
            teiMapManager.onStart();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule(this, tEType, initialProgram)).inject(this);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        binding.setNeedsSearch(needsSearch);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        binding.setTotalFiltersSearch(presenter.getQueryData().size());

        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
            fromRelationshipTeiUid = getIntent().getStringExtra("FROM_RELATIONSHIP_TEI");
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

        ViewExtensionsKt.clipWithRoundedCorners(binding.scrollView, ExtensionsKt.getDp(16));
        if (fromRelationship) {
            relationshipLiveAdapter = new RelationshipLiveAdapter(presenter, getSupportFragmentManager());
            binding.scrollView.setAdapter(relationshipLiveAdapter);
        } else {
            liveAdapter = new SearchTeiLiveAdapter(presenter, getSupportFragmentManager());
            binding.scrollView.setAdapter(liveAdapter);
        }

        binding.formRecycler.setAdapter(new FormAdapter(getSupportFragmentManager(), this, presenter));
        binding.enrollmentButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.requestFocus();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.clearFocus();
                v.performClick();
            }
            return true;
        });

        filtersAdapter.addEnrollmentStatus();
        filtersAdapter.addEventStatus();
        try {
            binding.filterLayout.setAdapter(filtersAdapter);

        } catch (Exception e) {
            Timber.e(e);
        }

        binding.mapLayerButton.setOnClickListener(view -> {
            new MapLayerDialog(teiMapManager.mapLayerManager)
                    .show(getSupportFragmentManager(), MapLayerDialog.class.getName());
        });

        carouselAdapter = new CarouselAdapter.Builder()
                .addOnTeiClickListener(
                        (teiUid, enrollmentUid, isDeleted) -> {
                            if (binding.mapCarousel.getCarouselEnabled()) {
                                updateTei = teiUid;
                                presenter.onTEIClick(teiUid, enrollmentUid, isDeleted);
                            }
                            return true;
                        })
                .addOnSyncClickListener(
                        teiUid -> {
                            if (binding.mapCarousel.getCarouselEnabled()) {
                                presenter.onSyncIconClick(teiUid);
                            }
                            return true;
                        })
                .addOnDeleteRelationshipListener(relationshipUid -> {
                    if (binding.mapCarousel.getCarouselEnabled()) {
                        presenter.deleteRelationship(relationshipUid);
                    }
                    return true;
                })
                .addOnRelationshipClickListener(teiUid -> {
                    if (binding.mapCarousel.getCarouselEnabled()) {
                        presenter.onTEIClick(teiUid, null, false);
                    }
                    return true;
                })
                .addOnEventClickListener((teiUid, enrollmentUid, eventUid) -> {
                    if (binding.mapCarousel.getCarouselEnabled()) {
                        updateTei = teiUid;
                        updateEvent = eventUid;
                        presenter.onTEIClick(teiUid, enrollmentUid, false);
                    }
                    return true;
                })
                .addOnProfileImageClickListener(
                        path -> {
                            if (binding.mapCarousel.getCarouselEnabled()) {
                                new ImageDetailBottomDialog(
                                        null,
                                        new File(path)
                                ).show(
                                        getSupportFragmentManager(),
                                        ImageDetailBottomDialog.TAG
                                );
                            }
                            return Unit.INSTANCE;
                        }
                )
                .addProgram(presenter.getProgram())
                .build();
        binding.mapCarousel.setAdapter(carouselAdapter);

        binding.executePendingBindings();
        showHideFilter();

        if (savedInstanceState != null) {
            presenter.restoreQueryData((HashMap<String, String>) savedInstanceState.getSerializable(Constants.QUERY_DATA));
        }
        updateFiltersSearch(presenter.getQueryData().size());

        teiMapManager = new TeiMapManager(binding.mapView);
        teiMapManager.setTeiFeatureType(presenter.getTrackedEntityType(tEType).featureType());
        teiMapManager.setEnrollmentFeatureType(presenter.getProgram() != null ? presenter.getProgram().featureType() : null);
        teiMapManager.setCarouselAdapter(carouselAdapter);
        teiMapManager.setOnMapClickListener(this);

        binding.mapCarousel.attachToMapManager(teiMapManager, () -> true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMapVisible()) {
            animations.initMapLoading(binding.mapCarousel);
            binding.toolbarProgress.show();
            binding.progressLayout.setVisibility(View.GONE);
            if (updateTei != null) {
                if (updateEvent != null) {
                    ((CarouselAdapter) binding.mapCarousel.getAdapter()).updateItem(presenter.getEventInfo(updateEvent, updateTei));
                } else {
                    ((CarouselAdapter) binding.mapCarousel.getAdapter()).updateItem(presenter.getTeiInfo(updateTei));
                }
                updateEvent = null;
                updateTei = null;
            }
            animations.endMapLoading(binding.mapCarousel);
            binding.toolbarProgress.hide();
        }
        if (initSearchNeeded) {
            presenter.init(tEType);
        } else {
            initSearchNeeded = true;
        }
        if (teiMapManager != null) {
            teiMapManager.onResume();
        }
        FilterManager.getInstance().clearUnsupportedFilters();
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        if (initSearchNeeded) {
            presenter.onDestroy();
        }
        if (teiMapManager != null) {
            teiMapManager.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (teiMapManager != null) {
            teiMapManager.onDestroy();
        }
        presenter.onDestroy();

        FilterManager.getInstance().clearEnrollmentStatus();
        FilterManager.getInstance().clearEventStatus();
        FilterManager.getInstance().clearEnrollmentDate();
        FilterManager.getInstance().clearSorting();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!ExtensionsKt.isKeyboardOpened(this)) {
            super.onBackPressed();
        } else {
            hideKeyboard();
        }
    }

    @Override
    public void onBackClicked() {
        hideKeyboard();
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
        outState.putSerializable(Constants.QUERY_DATA, presenter.getQueryData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case FilterManager.OU_TREE:
                if (resultCode == Activity.RESULT_OK) {
                    filtersAdapter.notifyDataSetChanged();
                    updateFilters(FilterManager.getInstance().getTotalFilters());
                }
                break;
            case Constants.RQ_QR_SCANNER:
                if (resultCode == RESULT_OK) {
                    scanTextView.updateScanResult(data.getStringExtra(Constants.EXTRA_DATA));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_PERMISSION_REQUEST) {
            initSearchNeeded = false;
        }
    }

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        initSearchNeeded = false;
        super.onMapPositionClick(coordinatesView);
    }

    @Override
    public void onsScanClicked(Intent intent, @NotNull ScanTextView scanTextView) {
        initSearchNeeded = false;
        super.onsScanClicked(intent, scanTextView);
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public void updateFiltersSearch(int totalFilters) {
        binding.setTotalFiltersSearch(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public Consumer<FeatureType> featureType() {
        return featureType -> this.featureType = featureType;
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
        popupMenu.getMenuInflater().inflate(R.menu.search_menu, popupMenu.getMenu());
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
                    if (backDropActive) {
                        closeFilters();
                    }
                    showMap(true);
                    break;
                default:
                    break;
            }
            return false;
        });

        boolean messageIsVisible = binding.messageContainer.getVisibility() == View.VISIBLE;
        boolean progressIsVisible = binding.progressLayout.getVisibility() == View.VISIBLE;
        boolean mapIsVisible = binding.mapView.getVisibility() == View.VISIBLE;
        boolean teiListIsVisible = binding.scrollView.getVisibility() == View.VISIBLE;


        popupMenu.getMenu().getItem(0).setVisible(!messageIsVisible && !mapIsVisible && featureType != FeatureType.NONE);
        popupMenu.getMenu().getItem(1).setVisible(!messageIsVisible && !teiListIsVisible && featureType != FeatureType.NONE);
        if (!progressIsVisible)
            popupMenu.show();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

    private void showMap(boolean showMap) {
        binding.scrollView.setVisibility(showMap ? View.GONE : View.VISIBLE);
        binding.mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);
        binding.mapCarousel.setVisibility(showMap ? View.VISIBLE : View.GONE);

        if (showMap) {
            binding.toolbarProgress.setVisibility(View.VISIBLE);
            binding.toolbarProgress.show();
            presenter.getMapData();
        } else {
            binding.mapLayerButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void setForm(List<TrackedEntityAttribute> trackedEntityAttributes, @Nullable Program program, HashMap<String, String> queryData,
                        List<ValueTypeDeviceRendering> renderingTypes) {
        //Form has been set.
        FormAdapter formAdapter = (FormAdapter) binding.formRecycler.getAdapter();
        formAdapter.setList(trackedEntityAttributes, program, queryData, renderingTypes);
        updateFiltersSearch(queryData.size());
    }

    @NonNull
    public Flowable<RowAction> rowActionss() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableRA();
    }

    @Override
    public void clearData() {
        if (!isMapVisible()) {
            binding.progressLayout.setVisibility(View.VISIBLE);
        }
        binding.scrollView.setVisibility(View.GONE);
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
    public void setTutorial() {
        new Handler().postDelayed(() ->
                        HelpManager.getInstance().show(getActivity(),
                                HelpManager.TutorialName.TEI_SEARCH,
                                null),
                500);
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST

    @Override
    public void setLiveData(LiveData<PagedList<SearchTeiModel>> liveData) {
        if (!fromRelationship) {
            liveData.observe(this, searchTeiModels -> {
                Trio<PagedList<SearchTeiModel>, String, Boolean> data = presenter.getMessage(searchTeiModels);
                presenter.checkFilters(data.val1().isEmpty());
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    liveAdapter.submitList(data.val0());
                    binding.progressLayout.setVisibility(View.GONE);
                    CountingIdlingResourceSingleton.INSTANCE.decrement();
                } else {
                    showMap(false);
                    binding.progressLayout.setVisibility(View.GONE);
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                    binding.scrollView.setVisibility(View.GONE);
                    CountingIdlingResourceSingleton.INSTANCE.decrement();
                }
            });
        } else {
            liveData.observeForever(searchTeiModels -> {
                Trio<PagedList<SearchTeiModel>, String, Boolean> data = presenter.getMessage(searchTeiModels);
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    relationshipLiveAdapter.submitList(data.val0());
                    binding.progressLayout.setVisibility(View.GONE);
                } else {
                    binding.progressLayout.setVisibility(View.GONE);
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                    binding.scrollView.setVisibility(View.GONE);
                }
                CountingIdlingResourceSingleton.INSTANCE.decrement();
                if (!presenter.getQueryData().isEmpty() && data.val2())
                    setFabIcon(false);
            });
        }
    }

    @Override
    public void setFiltersVisibility(boolean showFilters) {
        binding.filterCounter.setVisibility(showFilters ? View.VISIBLE : View.GONE);
        binding.searchFilterGeneral.setVisibility(showFilters ? View.VISIBLE : View.GONE);
    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;
        if (uid == null)
            binding.programSpinner.setSelection(0);
    }
    //endregion

    @Override
    public void setPrograms(List<Program> programs) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programs, presenter.getTrackedEntityName().displayName()));
        if (initialProgram != null && !initialProgram.isEmpty())
            setInitialProgram(programs);
        else
            binding.programSpinner.setSelection(0);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(binding.programSpinner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
        binding.programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (pos > 0) {
                    analyticsHelper().setEvent(CHANGE_PROGRAM, CLICK, CHANGE_PROGRAM);
                    Program selectedProgram = (Program) adapterView.getItemAtPosition(pos - 1);
                    updateMapVisibility(selectedProgram);
                    setProgramColor(presenter.getProgramColor(selectedProgram.uid()));
                    presenter.setProgram((Program) adapterView.getItemAtPosition(pos - 1));
                    String enrollmentDateLabel = selectedProgram.enrollmentDateLabel();
                    filtersAdapter.addEnrollmentDate(enrollmentDateLabel != null ? enrollmentDateLabel : getString(R.string.enrollment_date));
                } else if (programs.size() == 1 && pos != 0) {
                    updateMapVisibility(programs.get(0));
                    presenter.setProgram(programs.get(0));
                    String enrollmentDateLabel = programs.get(0).enrollmentDateLabel();
                    filtersAdapter.addEnrollmentDate(enrollmentDateLabel != null ? enrollmentDateLabel : getString(R.string.enrollment_date));
                } else {
                    updateMapVisibility(null);
                    presenter.setProgram(null);
                    filtersAdapter.removeEnrollmentDate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        teiMapManager.setMapStyle(
                new MapStyle(
                        presenter.getTEIColor(),
                        presenter.getSymbolIcon(),
                        presenter.getEnrollmentColor(),
                        presenter.getEnrollmentSymbolIcon(),
                        presenter.getProgramStageStyle(),
                        ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY_DARK)
                ));
    }

    private void updateMapVisibility(Program newProgram) {
        String currentProgram = presenter.getProgram() != null ? presenter.getProgram().uid() : null;
        String selectedProgram = newProgram != null ? newProgram.uid() : null;
        boolean programChanged = !Objects.equals(currentProgram, selectedProgram);
        if (isMapVisible() && programChanged) {
            showMap(false);
        }
    }

    private void setInitialProgram(List<Program> programs) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).uid().equals(initialProgram)) {
                binding.programSpinner.setSelection(i + 1);
            }
        }
    }

    @Override
    public void showAssignmentFilter() {
        filtersAdapter.addAssignedToMe();
    }

    @Override
    public void hideAssignmentFilter() {
        filtersAdapter.removeAssignedToMe();
    }

    @Override
    public void setProgramColor(String color) {
        int programTheme = ColorUtils.getThemeFromColor(color);
        int programColor = ColorUtils.getColorFrom(color, ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));


        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
            binding.enrollmentButton.setBackgroundTintList(ColorStateList.valueOf(programColor));
            binding.mainToolbar.setBackgroundColor(programColor);
            binding.backdropLayout.setBackgroundColor(programColor);
        } else {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
            int colorPrimary;
            switch (prefs.getInt(Constants.THEME, R.style.AppTheme)) {
                case R.style.RedTheme:
                    colorPrimary = R.color.colorPrimaryRed;
                    break;
                case R.style.OrangeTheme:
                    colorPrimary = R.color.colorPrimaryOrange;
                    break;
                case R.style.GreenTheme:
                    colorPrimary = R.color.colorPrimaryGreen;
                    break;
                default:
                    colorPrimary = R.color.colorPrimary;
                    break;
            }
            binding.enrollmentButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorPrimary)));
            binding.mainToolbar.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
            binding.backdropLayout.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
        }

        setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));
        binding.executePendingBindings();
        binding.clearFilter.setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                        binding.clearFilter.getDrawable(),
                        ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)
                ));
        binding.closeFilter.setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                        binding.closeFilter.getDrawable(),
                        ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)
                ));
        binding.progress.setIndeterminateDrawable(
                ColorUtils.tintDrawableWithColor(
                        binding.progress.getIndeterminateDrawable(),
                        ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)
                ));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue typedValue = new TypedValue();
            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
            int colorToReturn = a.getColor(0, 0);
            a.recycle();
            window.setStatusBarColor(colorToReturn);
        }
    }

    @Override
    public String fromRelationshipTEI() {
        return fromRelationshipTeiUid;
    }

    @Override
    public void setFabIcon(boolean needsSearch) {
        this.needsSearch.set(needsSearch);
        animSearchFab(needsSearch);
    }

    private void animSearchFab(boolean hasQuery) {
        if (hasQuery) {
            PropertyValuesHolder scalex = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f);
            PropertyValuesHolder scaley = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
            animation = ObjectAnimator.ofPropertyValuesHolder(binding.enrollmentButton, scalex, scaley);
            animation.setRepeatCount(ValueAnimator.INFINITE);
            animation.setRepeatMode(ValueAnimator.REVERSE);
            animation.setDuration(500);
            animation.start();
        } else {
            if (animation != null) {
                animation.cancel();
            }
            hideKeyboard();
        }
    }

    @Override
    public void showHideFilter() {
        binding.filterLayout.setVisibility(View.GONE);
        binding.formRecycler.setVisibility(View.VISIBLE);

        swipeFilters(false);
    }

    @Override
    public void showHideFilterGeneral() {
        binding.filterLayout.setVisibility(View.VISIBLE);
        binding.formRecycler.setVisibility(View.GONE);

        swipeFilters(true);
    }

    private void swipeFilters(boolean general) {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        if (backDropActive && !general && switchOpenClose == 0)
            switchOpenClose = 1;
        else if (backDropActive && general && switchOpenClose == 1)
            switchOpenClose = 0;
        else {
            switchOpenClose = general ? 0 : 1;
            backDropActive = !backDropActive;
        }
        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            activeFilter(general);
    }

    private void activeFilter(boolean general) {
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);

        if (backDropActive) {
            initSet.connect(R.id.mainLayout, ConstraintSet.TOP, general ? R.id.filterLayout : R.id.form_recycler, ConstraintSet.BOTTOM, 50);
        } else {
            initSet.connect(R.id.mainLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }

        setFabVisibility(backDropActive);
        setCarouselVisibility(backDropActive);

        initSet.applyTo(binding.backdropLayout);
    }

    private void setFabVisibility(boolean backDropActive) {
        binding.enrollmentButton.animate()
                .setDuration(500)
                .translationX(backDropActive ? 0 : 500)
                .start();
    }

    private void setCarouselVisibility(boolean backDropActive) {
        binding.mapCarousel.animate()
                .setDuration(500)
                .translationY(backDropActive ? 600 : 0)
                .start();
    }

    @Override
    public void closeFilters() {
        if (switchOpenClose == 0)
            showHideFilterGeneral();
        else
            showHideFilter();
    }

    @Override
    public void clearFilters() {
        if (switchOpenClose == 0) {
            FilterManager.getInstance().clearAllFilters();
            filtersAdapter.notifyDataSetChanged();
        } else
            presenter.onClearClick();

    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(this, OUTreeActivity.class);
        Bundle bundle = OUTreeActivity.Companion.getBundle(initialProgram);
        ouTreeIntent.putExtras(bundle);
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }

    @Override
    public void showPeriodRequest(Pair<FilterManager.PeriodRequest, Filters> periodRequest) {
        if (periodRequest.getFirst() == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().showFromToSelector(this, datePeriod -> {
                if (periodRequest.getSecond() == Filters.PERIOD) {
                    FilterManager.getInstance().addPeriod(datePeriod);
                } else {
                    FilterManager.getInstance().addEnrollmentPeriod(datePeriod);
                }
            });
        } else {
            DateUtils.getInstance().showPeriodDialog(this, datePeriods -> {
                        if (periodRequest.getSecond() == Filters.PERIOD) {
                            FilterManager.getInstance().addPeriod(datePeriods);
                        } else {
                            FilterManager.getInstance().addEnrollmentPeriod(datePeriods);
                        }
                    },
                    true);
        }
    }

    @Override
    public void openDashboard(String teiUid, String programUid, String enrollmentUid) {
        if (downloadingSnackbar != null && downloadingSnackbar.isShown()) {
            downloadingSnackbar.dismiss();
        }
        startActivity(TeiDashboardMobileActivity.intent(this, teiUid, enrollmentUid != null ? programUid : null, enrollmentUid));
    }

    @Override
    public void couldNotDownload(String typeName) {
        displayMessage(getString(R.string.download_tei_error, typeName));
    }

    @Override
    public void goToEnrollment(String enrollmentUid, String programUid) {
        Intent intent = EnrollmentActivity.Companion.getIntent(this,
                enrollmentUid,
                programUid,
                EnrollmentActivity.EnrollmentMode.NEW,
                fromRelationshipTEI() != null);
        startActivity(intent);
    }

    /*region MAP*/
    @Override
    public void setMap(List<SearchTeiModel> teis, HashMap<String, FeatureCollection> teiFeatureCollections, BoundingBox boundingBox, EventsByProgramStage events, List<EventUiComponentModel> eventUiComponentModels) {
        binding.progressLayout.setVisibility(View.GONE);

        sources = teiFeatureCollections.keySet();
        eventSources = events.component2().keySet();
        List<CarouselItemModel> allItems = new ArrayList<>();
        allItems.addAll(teis);
        allItems.addAll(eventUiComponentModels);
        for (SearchTeiModel searchTeiModel : teis) {
            allItems.addAll(new MapRelationshipToRelationshipMapModel().mapList(searchTeiModel.getRelationships()));
        }

        teiMapManager.init(() -> {
            teiMapManager.update(teiFeatureCollections, events, boundingBox);
            updateCarousel(allItems);
            binding.mapLayerButton.setVisibility(View.VISIBLE);
            return Unit.INSTANCE;
        });

        animations.endMapLoading(binding.mapCarousel);
        binding.toolbarProgress.hide();
    }

    private void updateCarousel(List<CarouselItemModel> allItems) {
        if (binding.mapCarousel.getAdapter() != null) {
            ((CarouselAdapter) binding.mapCarousel.getAdapter()).updateAllData(allItems);
        }
    }


    @Override
    public Consumer<D2Progress> downloadProgress() {
        return progress -> Snackbar.make(binding.getRoot(), getString(R.string.downloading), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean isMapVisible() {
        return binding.mapView.getVisibility() == View.VISIBLE;
    }


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointf = teiMapManager.getMap().getProjection().toScreenLocation(point);
        RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);

        Pair<List<String>, List<String[]>> sourcesAndLayer = teiMapManager.getSourcesAndLayersForSearch();
        return findFeature(rectF, sourcesAndLayer.component1(), sourcesAndLayer.component2(), 0);
    }

    private boolean findFeature(RectF rectF, List<String> sources, List<String[]> layers, int count) {
        String source = sources.get(count);
        String[] layersToSearch = layers.get(count);
        List<Feature> features = teiMapManager.getMap().queryRenderedFeatures(rectF, layersToSearch);
        if (!features.isEmpty()) {
            teiMapManager.mapLayerManager.selectFeature(null);
            Feature selectedFeature = features.get(0);
            if (source.contains("RELATIONSHIP")) {
                selectedFeature = teiMapManager.findFeature(source, RELATIONSHIP_UID, selectedFeature.getStringProperty(RELATIONSHIP_UID));
            }
            teiMapManager.mapLayerManager.getLayer(source, true).setSelectedItem(selectedFeature);
            binding.mapCarousel.scrollToFeature(selectedFeature);
            return true;
        } else if (count < sources.size() - 1) {
            return findFeature(rectF, sources, layers, count + 1);
        } else {
            return false;
        }
    }

    /*endregion*/
}
