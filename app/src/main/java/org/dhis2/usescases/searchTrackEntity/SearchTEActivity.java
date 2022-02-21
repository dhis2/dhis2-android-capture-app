package org.dhis2.usescases.searchTrackEntity;

import static android.view.View.GONE;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST;
import static org.dhis2.utils.analytics.AnalyticsConstants.CHANGE_PROGRAM;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;

import com.google.android.material.snackbar.Snackbar;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.commons.data.CarouselItemModel;
import org.dhis2.commons.data.SearchTeiModel;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.Filters;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.commons.idlingresource.CountingIdlingResourceSingleton;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.data.forms.dataentry.FormView;
import org.dhis2.data.forms.dataentry.ProgramAdapter;
import org.dhis2.data.location.LocationProvider;
import org.dhis2.databinding.ActivitySearchBinding;
import org.dhis2.form.data.FormRepository;
import org.dhis2.form.model.DispatcherProvider;
import org.dhis2.form.ui.FieldViewModelFactory;
import org.dhis2.maps.ExternalMapNavigation;
import org.dhis2.maps.carousel.CarouselAdapter;
import org.dhis2.maps.layer.MapLayerDialog;
import org.dhis2.maps.managers.TeiMapManager;
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel;
import org.dhis2.maps.model.MapStyle;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.searchTrackEntity.listView.SearchTEList;
import org.dhis2.usescases.searchTrackEntity.mapView.SearchTEMap;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.customviews.BreakTheGlassBottomDialog;
import org.dhis2.utils.customviews.ImageDetailBottomDialog;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment;
import io.reactivex.functions.Consumer;
import kotlin.Pair;
import kotlin.Unit;
import timber.log.Timber;

public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View, OnOrgUnitSelectionFinished {

    ActivitySearchBinding binding;
    @Inject
    SearchTEContractsModule.Presenter presenter;

    @Inject
    FiltersAdapter filtersAdapter;

    @Inject
    FieldViewModelFactory fieldViewModelFactory;
    @Inject
    FormRepository formRepository;
    @Inject
    LocationProvider locationProvider;
    @Inject
    DispatcherProvider dispatchers;
    @Inject
    NavigationPageConfigurator pageConfigurator;
    @Inject
    SearchTeiViewModelFactory viewModelFactory;

    private String initialProgram;
    private String tEType;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;
    private boolean backDropActive;
    private boolean fromAnalytics = false;

    private SearchTEIViewModel viewModel;
    /**
     * 0 - it is general filter
     * 1 - it is search filter
     * 2 - it was closed
     */
    private int switchOpenClose = 2;

    ObservableBoolean needsSearch = new ObservableBoolean(true);
    ObservableBoolean showClear = new ObservableBoolean(false);

    public boolean initSearchNeeded = true;
    private FormView formView;
    public SearchTEComponent searchComponent;

    private enum Extra {
        TEI_UID("TRACKED_ENTITY_UID"),
        PROGRAM_UID("PROGRAM_UID"),
        QUERY_ATTR("QUERY_DATA_ATTR"),
        QUERY_VALUES("QUERY_DATA_VALUES");
        private final String key;

        Extra(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    //---------------------------------------------------------------------------------------------

    //region LIFECYCLE
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");

        searchComponent = ((App) getApplicationContext()).userComponent().plus(
                new SearchTEModule(this,
                        tEType,
                        initialProgram,
                        getContext(),
                        SearchTEExtraKt.queryDataExtra(this)
                ));
        searchComponent.inject(this);

        initSearchForm();

        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, viewModelFactory).get(SearchTEIViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        binding.setNeedsSearch(needsSearch);
        binding.setShowClear(showClear);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());

        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
            fromRelationshipTeiUid = getIntent().getStringExtra("FROM_RELATIONSHIP_TEI");
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.formViewContainer, formView).commit();

        binding.searchButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.requestFocus();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                hideKeyboard();
                v.clearFocus();
                viewModel.onSearchClick();
            }
            return true;
        });

        configureBottomNavigation();

        try {
            binding.filterRecyclerLayout.setAdapter(filtersAdapter);
        } catch (Exception e) {
            Timber.e(e);
        }

        binding.executePendingBindings();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setFabVisibility(false, false);
        }

        if (savedInstanceState != null) {
            viewModel.restoreQueryData((HashMap<String, String>) savedInstanceState.getSerializable(Constants.QUERY_DATA));
        }

        binding.syncButton.setVisibility(initialProgram != null ? View.VISIBLE : GONE);
        binding.syncButton.setOnClickListener(v -> {
            openSyncDialog();
        });

        observeScreenState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FilterManager.getInstance().clearUnsupportedFilters();

        binding.navigationBar.onResume();

        if (initSearchNeeded) {
            presenter.init(tEType);
        } else {
            initSearchNeeded = true;
        }

        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());

    }

    @Override
    protected void onPause() {
        presenter.setOpeningFilterToNone();
        if (initSearchNeeded) {
            presenter.onDestroy();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();

        FilterManager.getInstance().clearEnrollmentStatus();
        FilterManager.getInstance().clearEventStatus();
        FilterManager.getInstance().clearEnrollmentDate();
        FilterManager.getInstance().clearWorkingList(false);
        FilterManager.getInstance().clearSorting();
        FilterManager.getInstance().clearAssignToMe();
        FilterManager.getInstance().clearFollowUp();

        presenter.clearOtherFiltersIfWebAppIsConfig();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (switchOpenClose != 2) {
            closeFilters();
            viewModel.setPreviousScreen();
        } else if (!ExtensionsKt.isKeyboardOpened(this)) {
            super.onBackPressed();
        } else {
            hideKeyboard();
        }
    }

    @Override
    public void onBackClicked() {
        onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.QUERY_DATA, (Serializable) viewModel.getQueryData());
    }

    private void openSyncDialog() {
        SyncStatusDialog syncDialog = new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.PROGRAM)
                .setUid(initialProgram)
                .onDismissListener(hasChanged -> {
                    if (hasChanged) viewModel.refreshData();
                })
                .build();
        syncDialog.show(getSupportFragmentManager(), "PROGRAM_SYNC");
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
        viewModel.refreshData();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

    private void initSearchForm() {
        formView = new FormView.Builder()
                .repository(formRepository)
                .locationProvider(locationProvider)
                .dispatcher(dispatchers)
                .onItemChangeListener(action -> {
                    viewModel.updateQueryData(action);
                    return Unit.INSTANCE;
                })
                .activityForResultListener(() -> {
                    initSearchNeeded = false;
                    return Unit.INSTANCE;
                })
                .onFieldItemsRendered(isEmpty -> {
                    presenter.setAttributesEmpty(isEmpty);
                    return Unit.INSTANCE;
                })
                .needToForceUpdate(true)
                .factory(getSupportFragmentManager())
                .build();
    }

    private void configureBottomNavigation() {
        binding.navigationBar.pageConfiguration(pageConfigurator);
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            if (backDropActive) {
                closeFilters();
            }
            binding.mainComponent.setVisibility(View.VISIBLE);
            switch (item.getItemId()) {
                case R.id.navigation_list_view:
                    viewModel.setListScreen();
                    showList();
                    showSearchAndFilterButtons();
                    break;
                case R.id.navigation_map_view:
                    viewModel.setMapScreen();
                    showMap();
                    showSearchAndFilterButtons();
                    break;
                case R.id.navigation_analytics:
                    viewModel.setAnalyticsScreen();
                    fromAnalytics = true;
                    showAnalytics();
                    hideSearchAndFilterButtons();
                    break;
            }
            return true;
        });
    }

    private void showList() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainComponent, SearchTEList.Companion.get(fromRelationship)).commit();
    }

    private void showMap() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainComponent, SearchTEMap.Companion.get(fromRelationship, tEType)).commit();
    }

    private void showAnalytics() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainComponent, GroupAnalyticsFragment.Companion.forProgram(initialProgram)).commit();
    }

    public boolean shouldDisplayButton() {
        return (backDropActive && switchOpenClose == 1) || (!needsSearch.get() && !isMapVisible());
    }

    private void hideSearchAndFilterButtons() {
        binding.searchFilterGeneral.setVisibility(GONE);
        binding.filterCounter.setVisibility(GONE);
        if (OrientationUtilsKt.isLandscape()) {
            binding.searchButton.setVisibility(GONE);
        }
    }

    private void showSearchAndFilterButtons() {
        if (fromAnalytics) {
            fromAnalytics = false;
            binding.searchFilterGeneral.setVisibility(View.VISIBLE);
            binding.filterCounter.setVisibility(binding.getTotalFilters() > 0 ? View.VISIBLE : View.GONE);
            if (OrientationUtilsKt.isLandscape()) {
                binding.searchButton.setVisibility(View.VISIBLE);
            }
        }
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST

    private void observeScreenState() {
        viewModel.getScreenState().observe(this, screenState -> {
            switch (screenState.getScreenState()) {
                case NONE:
                    break;
                case LIST:
                    configureListScreen((SearchList) screenState);
                    break;
                case MAP:
                    break;
                case SEARCHING:
                    configureSearchScreen((SearchForm) screenState);
                    break;
                case ANALYTICS:
                    break;
            }
        });
    }

    private void configureListScreen(SearchList searchConfiguration) {
        if (switchOpenClose == 1) {
            showHideFilter();
        }else if(switchOpenClose == 0){
            showHideFilterGeneral();
        }

        syncButtonVisibility(true);
        setFiltersVisibility(true);
    }

    private void configureSearchScreen(SearchForm searchConfiguration) {
        if (switchOpenClose != 1) {
            showHideFilter();
        }

        if (searchConfiguration.getQueryHasData()) {
            binding.clearFilterSearchButton.show();
        } else {
            binding.clearFilterSearchButton.hide();
        }
        syncButtonVisibility(false);
        setFiltersVisibility(false);

        SearchJavaToComposeKt.setMinAttributesMessage(
                binding.minAttributeMessage,
                searchConfiguration
        );
    }
    
    private void syncButtonVisibility(boolean canBeDisplayed){
        binding.syncButton.setVisibility(canBeDisplayed ? View.VISIBLE : GONE);
    }

    @Override
    public void setFiltersVisibility(boolean showFilters) {
        binding.filterCounter.setVisibility(showFilters ? View.VISIBLE : GONE);
        binding.searchFilterGeneral.setVisibility(showFilters ? View.VISIBLE : GONE);
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
                    setProgramColor(presenter.getProgramColor(selectedProgram.uid()), selectedProgram.uid());
                } else if (programs.size() == 1 && pos != 0) {
                    Program selectedProgram = programs.get(0);
                    setProgramColor(presenter.getProgramColor(selectedProgram.uid()), selectedProgram.uid());
                } else {
                    setProgramColor(presenter.getTrackedEntityType(tEType).style().color(), null);
                    binding.navigationBar.hide();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void updateNavigationBar() {
        binding.navigationBar.pageConfiguration(pageConfigurator);
    }

    @Override
    public void displayMinNumberOfAttributesMessage(int minAttributes) {
        displayMessage(String.format(getString(R.string.search_min_num_attr), minAttributes));
    }

    @Override
    public void showSyncDialog(String teiUid) {
        SyncStatusDialog syncDialog = new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.TEI)
                .setUid(teiUid)
                .onDismissListener(hasChanged -> {
                    if (hasChanged) viewModel.refreshData();
                })
                .build();
        syncDialog.show(getSupportFragmentManager(), "TEI_SYNC");
    }

    private void setInitialProgram(List<Program> programs) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).uid().equals(initialProgram)) {
                binding.programSpinner.setSelection(i + 1);
            }
        }
    }

    @Override
    public void setProgramColor(String color, String programUid) {
        int programTheme = ColorUtils.getThemeFromColor(color);

        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);

        if (prefs.getInt(Constants.PROGRAM_THEME, -1) == programTheme) return;

        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
        } else {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
        }

        Intent intent = new Intent(this, SearchTEActivity.class);
        if (fromRelationshipTeiUid != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        }
        intent.putExtras(updateBundle(programUid));
        startActivity(intent);
        finish();

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private Bundle updateBundle(String programUid) {
        Bundle bundle = getIntent().getExtras();
        bundle.putString(Extra.PROGRAM_UID.key(), programUid);
        Map<String, String> currentQueryData = viewModel.getQueryData();
        bundle.putStringArrayList(Extra.QUERY_ATTR.key(), new ArrayList<>(currentQueryData.keySet()));
        bundle.putStringArrayList(Extra.QUERY_VALUES.key(), new ArrayList<>(currentQueryData.values()));
        return bundle;
    }

    @Override
    public String fromRelationshipTEI() {
        return fromRelationshipTeiUid;
    }

    @Override
    public void setFabIcon(boolean needsSearch) {
        this.needsSearch.set(needsSearch);
    }

    @Override
    public void showHideFilter() {
        binding.filterRecyclerLayout.setVisibility(GONE);
        binding.formViewContainer.setVisibility(View.VISIBLE);

        swipeFilters(false);
    }

    @Override
    public void showHideFilterGeneral() {
        binding.filterRecyclerLayout.setVisibility(View.VISIBLE);
        binding.formViewContainer.setVisibility(GONE);

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
            int nextSwitchOpenClose = general ? 0 : 1;
            switchOpenClose = switchOpenClose != nextSwitchOpenClose ? nextSwitchOpenClose : 2;
            backDropActive = !backDropActive;
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            activeFilter(general);
        } else {
            binding.searchButton.setVisibility(general ? View.GONE : View.VISIBLE);
        }
    }

    private void activeFilter(boolean general) {
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);

        if (backDropActive) {
            initSet.connect(R.id.mainComponent, ConstraintSet.TOP, general ? R.id.filterRecyclerLayout : R.id.formViewContainer, ConstraintSet.BOTTOM, general ? ExtensionsKt.getDp(16) : 0);
        } else {
            initSet.connect(R.id.mainComponent, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }

        setFabVisibility(shouldDisplayButton(), !backDropActive || general);
        if (backDropActive) {
            binding.navigationBar.hide();
        } else {
            binding.navigationBar.show();
        }

        initSet.applyTo(binding.backdropLayout);
    }

    @Override
    public void setInitialFilters(List<FilterItem> filtersToDisplay) {
        filtersAdapter.submitList(filtersToDisplay);
    }

    @Override
    public void showClearSearch(boolean empty) {
        showClear.set(empty);
    }

    @Override
    public void hideFilter() {
        binding.searchFilterGeneral.setVisibility(GONE);
    }

    private void setFabVisibility(boolean show, boolean onNavBar) {
        binding.searchButton.animate()
                .setDuration(500)
                .translationX(show ? 0 : 500)
                .translationY(onNavBar ? -ExtensionsKt.getDp(56) : 0)
                .start();

        binding.clearFilterSearchButton.animate()
                .setDuration(500)
                .translationX(show && !onNavBar ? 0 : 500)
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
            filtersAdapter.notifyDataSetChanged();
            FilterManager.getInstance().clearAllFilters();
        } else {
            formView.clearValues();
            presenter.onClearClick();
        }
    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }

    @Override
    public void openOrgUnitTreeSelector() {
        OUTreeFragment ouTreeFragment = OUTreeFragment.Companion.newInstance(true, FilterManager.getInstance().getOrgUnitUidsFilters());
        ouTreeFragment.setSelectionCallback(this);
        ouTreeFragment.show(getSupportFragmentManager(), "OUTreeFragment");
    }

    @Override
    public void onSelectionFinished(List<? extends OrganisationUnit> selectedOrgUnits) {
        presenter.setOrgUnitFilters((List<OrganisationUnit>) selectedOrgUnits);
    }

    @Override
    public void showPeriodRequest(Pair<FilterManager.PeriodRequest, Filters> periodRequest) {
        if (periodRequest.getFirst() == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(this, datePeriod -> {
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
        FilterManager.getInstance().clearWorkingList(true);
        startActivity(TeiDashboardMobileActivity.intent(this, teiUid, enrollmentUid != null ? programUid : null, enrollmentUid));
    }

    @Override
    public void couldNotDownload(String typeName) {
        displayMessage(getString(R.string.download_tei_error, typeName));
    }

    @Override
    public void showBreakTheGlass(String teiUid, String enrollmentUid) {
        new BreakTheGlassBottomDialog()
                .setPositiveButton(reason -> {
                    presenter.downloadTeiWithReason(teiUid, enrollmentUid, reason);
                    return Unit.INSTANCE;
                })
                .show(getSupportFragmentManager(), BreakTheGlassBottomDialog.class.getName());
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

    @Override
    public Consumer<D2Progress> downloadProgress() {
        return progress -> Snackbar.make(binding.getRoot(), getString(R.string.downloading), Snackbar.LENGTH_SHORT).show();
    }

    private boolean isMapVisible() {
        return binding.navigationBar.getSelectedItemId() == R.id.navigation_map_view;
    }

    /*endregion*/
}
