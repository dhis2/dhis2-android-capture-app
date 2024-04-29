package org.dhis2.usescases.searchTrackEntity;

import static android.view.View.GONE;
import static org.dhis2.commons.extensions.ViewExtensionsKt.closeKeyboard;
import static org.dhis2.usescases.searchTrackEntity.searchparameters.SearchParametersScreenKt.initSearchScreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.compose.animation.ExperimentalAnimationApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.bindings.ExtensionsKt;
import org.dhis2.bindings.ViewExtensionsKt;
import org.dhis2.commons.Constants;
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.Filters;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.sync.SyncContext;
import org.dhis2.data.forms.dataentry.ProgramAdapter;
import org.dhis2.databinding.ActivitySearchBinding;
import org.dhis2.form.ui.intent.FormIntent;
import org.dhis2.ui.ThemeManager;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.searchTrackEntity.listView.SearchTEList;
import org.dhis2.usescases.searchTrackEntity.mapView.SearchTEMap;
import org.dhis2.usescases.searchTrackEntity.ui.SearchScreenConfigurator;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.customviews.BreakTheGlassBottomDialog;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.utils.granularsync.SyncStatusDialogNavigatorKt;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment;
import io.reactivex.functions.Consumer;
import kotlin.Pair;
import kotlin.Unit;
import timber.log.Timber;

public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View {

    ActivitySearchBinding binding;
    SearchScreenConfigurator searchScreenConfigurator;

    @Inject
    SearchTEContractsModule.Presenter presenter;

    @Inject
    FiltersAdapter filtersAdapter;

    @Inject
    SearchTeiViewModelFactory viewModelFactory;

    @Inject
    SearchNavigator searchNavigator;

    @Inject
    NetworkUtils networkUtils;

    @Inject
    ThemeManager themeManager;

    @Inject
    FeatureConfigRepository featureConfig;

    @Inject
    ResourceManager resourceManager;

    private static final String INITIAL_PAGE = "initialPage";

    private String initialProgram;
    private String tEType;
    private Map<String, String> initialQuery;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;
    private boolean fromAnalytics = false;

    private SearchTEIViewModel viewModel;

    private boolean initSearchNeeded = true;
    public SearchTEComponent searchComponent;
    private int initialPage = 0;

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

    public static Intent getIntent(Context context, String programUid, String teiTypeToAdd, String teiUid, boolean fromRelationship) {
        Intent intent = new Intent(context, SearchTEActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean("FROM_RELATIONSHIP", fromRelationship);
        extras.putString("FROM_RELATIONSHIP_TEI", teiUid);
        extras.putString(Extra.TEI_UID.key, teiTypeToAdd);
        extras.putString(Extra.PROGRAM_UID.key, programUid);
        intent.putExtras(extras);
        return intent;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    @OptIn(markerClass = ExperimentalAnimationApi.class)
    protected void  onCreate(@Nullable Bundle savedInstanceState) {

        initializeVariables(savedInstanceState);
        inject();

        if (initialProgram != null) {
            themeManager.setProgramTheme(initialProgram);
        }
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, viewModelFactory).get(SearchTEIViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        initSearchParameters();

        searchScreenConfigurator = new SearchScreenConfigurator(
                binding,
                isOpen -> {
                    viewModel.setFiltersOpened(isOpen);
                    return Unit.INSTANCE;
                });
        if (savedInstanceState != null && savedInstanceState.containsKey(INITIAL_PAGE)) {
            initialPage = savedInstanceState.getInt(INITIAL_PAGE);
            binding.setNavigationInitialPage(initialPage);
        }
        binding.setPresenter(presenter);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());

        if (OrientationUtilsKt.isLandscape()) {
            viewModel.getFiltersOpened().observe(this, isOpened -> {
                if (Boolean.TRUE.equals(isOpened)) {
                    ViewExtensionsKt.clipWithRoundedCorners(binding.mainComponent, ExtensionsKt.getDp(16));
                } else {
                    ViewExtensionsKt.clipWithTopRightRoundedCorner(binding.mainComponent, ExtensionsKt.getDp(16));
                }
            });
        } else {
            ViewExtensionsKt.clipWithRoundedCorners(binding.mainComponent, ExtensionsKt.getDp(16));
        }

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
        observeScreenState();
        observeDownload();
        observeLegacyInteractions();

        if (SyncStatusDialogNavigatorKt.shouldLaunchSyncDialog(getIntent())) {
            openSyncDialog();
        }
    }

    private void initializeVariables(Bundle savedInstanceState) {
        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");
        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
            fromRelationshipTeiUid = getIntent().getStringExtra("FROM_RELATIONSHIP_TEI");
        } catch (Exception e) {
            Timber.d(e);
        }
        initialQuery = SearchTEExtraKt.queryDataExtra(this, savedInstanceState);
    }

    private void inject() {
        searchComponent = ((App) getApplicationContext()).userComponent().plus(
                new SearchTEModule(this,
                        tEType,
                        initialProgram,
                        getContext(),
                        initialQuery
                ));
        searchComponent.inject(this);
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
        FilterManager.getInstance().clearWorkingList(true);
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
        outState.putInt(INITIAL_PAGE, binding.navigationBar.currentPage());
    }

    private void openSyncDialog() {
        View contextView = findViewById(R.id.navigationBar);
        new SyncStatusDialog.Builder()
                .withContext(this, null)
                .withSyncContext(
                        new SyncContext.TrackerProgram(initialProgram)
                )
                .onDismissListener(hasChanged -> {
                    if (hasChanged) viewModel.refreshData();
                })
                .onNoConnectionListener(() ->
                        Snackbar.make(
                                contextView,
                                R.string.sync_offline_check_connection,
                                Snackbar.LENGTH_SHORT
                        ).show()
                )
                .show("PROGRAM_SYNC");
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
        viewModel.updateActiveFilters(totalFilters > 0);
        viewModel.refreshData();
    }

    private void initSearchParameters() {
        initSearchScreen(
                binding.searchContainer,
                viewModel,
                initialProgram,
                tEType,
                resourceManager,
                (uid, preselectedOrgUnits, orgUnitScope, label) -> {
                    new OUTreeFragment.Builder()
                            .showAsDialog()
                            .withPreselectedOrgUnits(preselectedOrgUnits)
                            .singleSelection()
                            .onSelection(selectedOrgUnits -> {
                                String selectedOrgUnit = null;
                                if (!selectedOrgUnits.isEmpty()) {
                                    selectedOrgUnit = selectedOrgUnits.get(0).uid();
                                }
                                viewModel.onParameterIntent(
                                        new FormIntent.OnSave(
                                                uid,
                                                selectedOrgUnit,
                                                ValueType.ORGANISATION_UNIT,
                                                null
                                        )
                                );
                                return Unit.INSTANCE;
                            })
                            .orgUnitScope(orgUnitScope)
                            .build()
                            .show(getSupportFragmentManager(), label);
                    return Unit.INSTANCE;
                },
                () -> {
                    closeKeyboard(binding.root);
                    presenter.onClearClick();
                    return Unit.INSTANCE;
                }
        );
    }

    private void configureBottomNavigation() {
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            if (viewModel.searchOrFilterIsOpen()) {
                searchScreenConfigurator.closeBackdrop();
            }
            binding.mainComponent.setVisibility(View.VISIBLE);
            switch (item.getItemId()) {
                case R.id.navigation_list_view -> {
                    viewModel.setListScreen();
                    showList();
                    showSearchAndFilterButtons();
                }
                case R.id.navigation_map_view -> networkUtils.performIfOnline(
                        this,
                        () -> {
                            presenter.trackSearchMapVisualization();
                            viewModel.setMapScreen();
                            showMap();
                            showSearchAndFilterButtons();
                            return null;
                        },
                        () -> {
                            binding.navigationBar.selectItemAt(0);
                            return null;
                        },
                        getString(R.string.msg_network_connection_maps)
                );
                case R.id.navigation_analytics -> {
                    presenter.trackSearchAnalytics();
                    viewModel.setAnalyticsScreen();
                    fromAnalytics = true;
                    showAnalytics();
                    hideSearchAndFilterButtons();
                }
            }
            return true;
        });

        viewModel.getPageConfiguration().observe(this, pageConfigurator -> {
            if (initialPage == 0) {
                showList();
            }
            binding.navigationBar.setOnConfigurationFinishListener(() -> {
                if (viewModel.searchOrFilterIsOpen()) {
                    binding.navigationBar.hide();
                } else {
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
        viewModel.getRefreshData().observe(this, refresh -> {
            closeKeyboard(binding.root);
        });
    }

    private void showMap() {
        if (currentContent != Content.MAP) {
            currentContent = Content.MAP;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.mainComponent, SearchTEMap.Companion.get(fromRelationship, tEType)).commit();
            observeMapLoading();
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
    }

    private void showSearchAndFilterButtons() {
        if (fromAnalytics) {
            fromAnalytics = false;
            binding.searchFilterGeneral.setVisibility(View.VISIBLE);
            binding.filterCounter.setVisibility(binding.getTotalFilters() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void observeScreenState() {
        viewModel.getScreenState().observe(this, screenState ->
                searchScreenConfigurator.configure(screenState));
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
                        teiUid -> {
                            couldNotDownload(presenter.getTrackedEntityName().displayName());
                            return Unit.INSTANCE;
                        },
                        errorMessage -> {
                            displayMessage(errorMessage);
                            return Unit.INSTANCE;
                        }
                ));
    }

    private void observeLegacyInteractions() {
        viewModel.getLegacyInteraction().observe(this, legacyInteraction -> {
            if (legacyInteraction != null) {
                switch (legacyInteraction.getId()) {
                    case ON_ENROLL_CLICK -> {
                        LegacyInteraction.OnEnrollClick interaction = (LegacyInteraction.OnEnrollClick) legacyInteraction;
                        presenter.onEnrollClick(new HashMap<>(interaction.getQueryData()));
                    }
                    case ON_ADD_RELATIONSHIP -> {
                        LegacyInteraction.OnAddRelationship interaction = (LegacyInteraction.OnAddRelationship) legacyInteraction;
                        presenter.addRelationship(interaction.getTeiUid(), interaction.getRelationshipTypeUid(), interaction.getOnline());
                    }
                    case ON_SYNC_CLICK -> {
                        LegacyInteraction.OnSyncIconClick interaction = (LegacyInteraction.OnSyncIconClick) legacyInteraction;
                        presenter.onSyncIconClick(interaction.getTeiUid());
                    }
                    case ON_ENROLL -> {
                        LegacyInteraction.OnEnroll interaction = (LegacyInteraction.OnEnroll) legacyInteraction;
                        presenter.enroll(
                                interaction.getInitialProgramUid(),
                                interaction.getTeiUid(),
                                new HashMap<>(interaction.getQueryData())
                        );
                    }
                    case ON_TEI_CLICK -> {
                        LegacyInteraction.OnTeiClick interaction = (LegacyInteraction.OnTeiClick) legacyInteraction;
                        presenter.onTEIClick(
                                interaction.getTeiUid(),
                                interaction.getEnrollmentUid(),
                                interaction.getOnline()
                        );
                    }
                }

                viewModel.onLegacyInteractionConsumed();
            }
        });
    }

    private void observeMapLoading() {
        viewModel.getRefreshData().observe(this, refresh -> {
            if (currentContent == Content.MAP) {
                binding.toolbarProgress.show();
            }
        });
        viewModel.getMapResults().observe(this, result -> binding.toolbarProgress.hide());
    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;
        if (uid == null)
            binding.programSpinner.setSelection(0);
    }

    @Override
    public void setPrograms(List<ProgramSpinnerModel> programs) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this,
                R.layout.spinner_program_layout,
                R.id.spinner_text,
                programs,
                presenter.getTrackedEntityName().displayName()));
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
    public void showSyncDialog(String enrollmentUid) {
        View contextView = findViewById(R.id.navigationBar);
        new SyncStatusDialog.Builder()
                .withContext(this, null)
                .withSyncContext(
                        new SyncContext.TrackerProgramTei(enrollmentUid)
                )
                .onDismissListener(hasChanged -> {
                    if (hasChanged) viewModel.refreshData();
                })
                .onNoConnectionListener(() ->
                        Snackbar.make(
                                contextView,
                                R.string.sync_offline_check_connection,
                                Snackbar.LENGTH_SHORT
                        ).show()
                ).show("TEI_SYNC");
    }

    private void setInitialProgram(List<ProgramSpinnerModel> programs) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).getUid().equals(initialProgram)) {
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
        }
    }

    @Override
    public void openOrgUnitTreeSelector() {
        new OUTreeFragment.Builder()
                .showAsDialog()
                .withPreselectedOrgUnits(
                        FilterManager.getInstance().getOrgUnitUidsFilters()
                )
                .onSelection(selectedOrgUnits -> {
                    presenter.setOrgUnitFilters((List<OrganisationUnit>) selectedOrgUnits);
                    return Unit.INSTANCE;
                })
                .build()
                .show(getSupportFragmentManager(), "OUTreeFragment");
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
                .setProgram(presenter.getProgram().uid())
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
        return progress -> Snackbar.make(
                binding.getRoot(),
                getString(R.string.downloading),
                BaseTransientBottomBar.LENGTH_SHORT
        ).show();
    }
}
