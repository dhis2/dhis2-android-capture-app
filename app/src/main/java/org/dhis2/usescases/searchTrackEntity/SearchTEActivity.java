package org.dhis2.usescases.searchTrackEntity;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.Filters;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished;
import org.dhis2.data.forms.dataentry.FormView;
import org.dhis2.data.forms.dataentry.ProgramAdapter;
import org.dhis2.databinding.ActivitySearchBinding;
import org.dhis2.databinding.SnackbarMinAttrBinding;
import org.dhis2.form.data.FormRepository;
import org.dhis2.form.model.DispatcherProvider;
import org.dhis2.form.ui.FieldViewModelFactory;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.searchTrackEntity.listView.SearchTEList;
import org.dhis2.usescases.searchTrackEntity.mapView.SearchTEMap;
import org.dhis2.usescases.searchTrackEntity.ui.SearchScreenConfigurator;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.customviews.BreakTheGlassBottomDialog;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment;
import io.reactivex.functions.Consumer;
import kotlin.Pair;
import kotlin.Unit;
import timber.log.Timber;

public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View, OnOrgUnitSelectionFinished {

    ActivitySearchBinding binding;
    SearchScreenConfigurator searchScreenConfigurator;

    @Inject
    SearchTEContractsModule.Presenter presenter;

    @Inject
    FiltersAdapter filtersAdapter;

    @Inject
    FieldViewModelFactory fieldViewModelFactory;
    @Inject
    FormRepository formRepository;
    @Inject
    DispatcherProvider dispatchers;

    @Inject
    SearchTeiViewModelFactory viewModelFactory;

    @Inject
    SearchNavigator searchNavigator;

    private String initialProgram;
    private String tEType;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;
    private boolean fromAnalytics = false;

    private SearchTEIViewModel viewModel;

    public boolean initSearchNeeded = true;
    private FormView formView;
    public SearchTEComponent searchComponent;

    public enum Extra {
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

    private enum Content {
        LIST,
        MAP,
        ANALYTICS
    }

    private Content currentContent = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        initializeVariables();
        inject(savedInstanceState);

        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, viewModelFactory).get(SearchTEIViewModel.class);

        initSearchForm();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        searchScreenConfigurator = new SearchScreenConfigurator(
                binding,
                isOpen -> {
                    viewModel.setFiltersOpened(isOpen);
                    return Unit.INSTANCE;
                });
        binding.setPresenter(presenter);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        ViewExtensionsKt.clipWithRoundedCorners(binding.mainComponent, ExtensionsKt.getDp(16));
        binding.searchButton.setOnClickListener(v -> {
            hideKeyboard();
            if (OrientationUtilsKt.isPortrait()) searchScreenConfigurator.closeBackdrop();
            formView.onEditionFinish();
            binding.backdropLayout.post(() ->
                    viewModel.onSearchClick(minNumberOfAttributes -> {
                        showSnackbar(
                                v,
                                String.format(getString(R.string.search_min_num_attr),
                                        minNumberOfAttributes),
                                getString(R.string.button_ok)

                        );
                        return Unit.INSTANCE;
                    }));
        });

        binding.filterRecyclerLayout.setAdapter(filtersAdapter);

        binding.executePendingBindings();

        binding.syncButton.setVisibility(initialProgram != null ? View.VISIBLE : GONE);
        binding.syncButton.setOnClickListener(v -> openSyncDialog());

        SearchJavaToComposeKt.setLandscapeOpenSearchButton(
                binding.landOpenSearchButton,
                viewModel,
                () -> {
                    viewModel.setSearchScreen();
                    return Unit.INSTANCE;
                }
        );

        configureBottomNavigation();
        showList();
        observeScreenState();
        observeDownload();
    }

    private void initializeVariables() {
        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");
        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
            fromRelationshipTeiUid = getIntent().getStringExtra("FROM_RELATIONSHIP_TEI");
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void inject(Bundle savedInstanceState) {
        searchComponent = ((App) getApplicationContext()).userComponent().plus(
                new SearchTEModule(this,
                        tEType,
                        initialProgram,
                        getContext(),
                        SearchTEExtraKt.queryDataExtra(this, savedInstanceState)
                ));
        searchComponent.inject(this);
    }

    private void showSnackbar(View view, String message, String actionText) {
        Snackbar snackbar = Snackbar.make(
                view,
                "",
                BaseTransientBottomBar.LENGTH_LONG
        );
        SnackbarMinAttrBinding snackbarBinding = SnackbarMinAttrBinding.inflate(getLayoutInflater());
        snackbarBinding.message.setText(message);
        snackbarBinding.actionButton.setOnClickListener(v -> {
            if (snackbar.isShown()) snackbar.dismiss();
        });
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);

        snackbarLayout.addView(snackbarBinding.getRoot(), 0);

        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FilterManager.getInstance().clearUnsupportedFilters();

        if (initSearchNeeded) {
            presenter.init();
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
        viewModel.onBackPressed(
                OrientationUtilsKt.isPortrait(),
                viewModel.searchOrFilterIsOpen(),
                ExtensionsKt.isKeyboardOpened(this),
                () -> {
                    super.onBackPressed();
                    return Unit.INSTANCE;
                },
                () -> {
                    if (viewModel.filterIsOpen()) {
                        showHideFilterGeneral();
                    }
                    viewModel.setPreviousScreen();
                    return Unit.INSTANCE;
                },
                () -> {
                    hideKeyboard();
                    return Unit.INSTANCE;
                }
        );
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
        viewModel.updateActiveFilters(totalFilters > 0);
        viewModel.refreshData();
    }

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
                .onFieldItemsRendered(isEmpty -> Unit.INSTANCE)
                .needToForceUpdate(true)
                .factory(getSupportFragmentManager())
                .build();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.formViewContainer, formView).commit();
    }

    private void configureBottomNavigation() {
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            if (viewModel.searchOrFilterIsOpen()) {
                searchScreenConfigurator.closeBackdrop();
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

        viewModel.getPageConfiguration().observe(this, pageConfigurator -> {
            binding.navigationBar.setOnConfigurationFinishListener(() -> {
                if (viewModel.canDisplayBottomNavigationBar()) {
                    binding.navigationBar.show();
                }
                return Unit.INSTANCE;
            });
            binding.navigationBar.pageConfiguration(pageConfigurator);
        });
    }

    private void showList() {
        if (currentContent != Content.LIST) {
            currentContent = Content.LIST;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.mainComponent, SearchTEList.Companion.get(fromRelationship)).commit();
        }
    }

    private void showMap() {
        if (currentContent != Content.MAP) {
            currentContent = Content.MAP;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.mainComponent, SearchTEMap.Companion.get(fromRelationship, tEType)).commit();
        }
    }

    private void showAnalytics() {
        if (currentContent != Content.ANALYTICS) {
            currentContent = Content.ANALYTICS;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.mainComponent, GroupAnalyticsFragment.Companion.forProgram(initialProgram)).commit();
        }
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
            binding.searchButton.setVisibility(OrientationUtilsKt.isLandscape() ? View.VISIBLE : GONE);
        }
    }

    private void observeScreenState() {
        viewModel.getScreenState().observe(this, screenState -> {
            searchScreenConfigurator.configure(screenState);
        });
    }

    private void observeDownload() {
        viewModel.getDownloadResult().observe(this, result ->
                result.handleResult(
                        (teiUid, programUid, enrollmentUid) -> {
                            openDashboard(teiUid,
                                    programUid,
                                    enrollmentUid);
                            return Unit.INSTANCE;
                        },
                        (teiUid, enrollmentUid) -> {
                            showBreakTheGlass(teiUid, enrollmentUid);
                            return Unit.INSTANCE;
                        },
                        (teiUid) -> {
                            couldNotDownload(presenter.getTrackedEntityName().displayName());
                            return Unit.INSTANCE;
                        },
                        (errorMessage) -> {
                            displayMessage(errorMessage);
                            return Unit.INSTANCE;
                        }
                ));
    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;
        if (uid == null)
            binding.programSpinner.setSelection(0);
    }

    @Override
    public void setPrograms(List<Program> programs) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programs, presenter.getTrackedEntityName().displayName()));
        if (initialProgram != null && !initialProgram.isEmpty())
            setInitialProgram(programs);
        else
            binding.programSpinner.setSelection(0);

        ViewExtensionsKt.overrideHeight(binding.programSpinner, 500);
        ViewExtensionsKt.doOnItemSelected(binding.programSpinner, selectedIndex -> {
            viewModel.onProgramSelected(selectedIndex, programs, selectedProgram -> {
                changeProgram(selectedProgram);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
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

    public void changeProgram(@Nullable String programUid) {
        searchNavigator.changeProgram(
                programUid,
                viewModel.queryDataByProgram(programUid),
                fromRelationshipTeiUid
        );
    }

    @Override
    public String fromRelationshipTEI() {
        return fromRelationshipTeiUid;
    }

    @Override
    public void showHideFilterGeneral() {
        viewModel.onFiltersClick(OrientationUtilsKt.isLandscape());
    }

    @Override
    public void setInitialFilters(List<FilterItem> filtersToDisplay) {
        filtersAdapter.submitList(filtersToDisplay);
    }

    @Override
    public void hideFilter() {
        binding.searchFilterGeneral.setVisibility(GONE);
    }

    @Override
    public void clearFilters() {
        if (viewModel.filterIsOpen()) {
            filtersAdapter.notifyDataSetChanged();
            FilterManager.getInstance().clearAllFilters();
        } else {
            formView.onEditionFinish();
            formView.clearValues();
            presenter.onClearClick();
        }
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
        searchNavigator.openDashboard(teiUid, programUid, enrollmentUid);
    }

    public void refreshData() {
        viewModel.refreshData();
    }

    @Override
    public void couldNotDownload(String typeName) {
        displayMessage(getString(R.string.download_tei_error, typeName));
    }

    @Override
    public void showBreakTheGlass(String teiUid, String enrollmentUid) {
        new BreakTheGlassBottomDialog()
                .setPositiveButton(reason -> {
                    viewModel.onDownloadTei(teiUid, enrollmentUid, reason);
                    return Unit.INSTANCE;
                })
                .show(getSupportFragmentManager(), BreakTheGlassBottomDialog.class.getName());
    }

    @Override
    public void goToEnrollment(String enrollmentUid, String programUid) {
        searchNavigator.goToEnrollment(enrollmentUid, programUid, fromRelationshipTEI());
    }

    @Override
    public Consumer<D2Progress> downloadProgress() {
        return progress -> Snackbar.make(binding.getRoot(), getString(R.string.downloading), Snackbar.LENGTH_SHORT).show();
    }
}
