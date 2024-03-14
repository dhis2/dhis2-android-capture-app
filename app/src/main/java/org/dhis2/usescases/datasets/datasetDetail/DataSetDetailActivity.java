package org.dhis2.usescases.datasets.datasetDetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.bindings.ExtensionsKt;
import org.dhis2.bindings.ViewExtensionsKt;
import org.dhis2.commons.Constants;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.sync.ConflictType;
import org.dhis2.databinding.ActivityDatasetDetailBinding;
import org.dhis2.ui.ThemeManager;
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.commons.sync.SyncContext;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.utils.granularsync.SyncStatusDialogNavigatorKt;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.List;

import javax.inject.Inject;

import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment;
import kotlin.Unit;


public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailView {

    private ActivityDatasetDetailBinding binding;
    private String dataSetUid;
    public DataSetDetailComponent dataSetDetailComponent;

    private Fragment fragment;

    @Inject
    DataSetDetailPresenter presenter;

    @Inject
    FilterManager filterManager;

    @Inject
    FiltersAdapter filtersAdapter;

    @Inject
    ThemeManager themeManager;

    @Inject
    DataSetDetailViewModelFactory viewModelFactory;

    private boolean backDropActive;

    private DataSetDetailViewModel viewModel;

    public static Intent intent(
            @NonNull Context context,
            @NonNull String dataSetUid) {
        Intent intent = new Intent(context, DataSetDetailActivity.class);
        intent.putExtra("DATASET_UID", dataSetUid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataSetUid = getIntent().getStringExtra("DATASET_UID");
        dataSetDetailComponent = ((App) getApplicationContext()).userComponent().plus(new DataSetDetailModule(this, dataSetUid));
        dataSetDetailComponent.inject(this);
        themeManager.setDataSetTheme(dataSetUid);
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, viewModelFactory).get(DataSetDetailViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);
        binding.setName(getIntent().getStringExtra(Constants.DATA_SET_NAME));
        binding.setPresenter(presenter);

        ViewExtensionsKt.clipWithRoundedCorners(binding.eventsLayout, ExtensionsKt.getDp(16));
        binding.filterLayout.setAdapter(filtersAdapter);
        configureBottomNavigation();

        if (SyncStatusDialogNavigatorKt.shouldLaunchSyncDialog(getIntent())) {
            showGranularSync();
        }
    }

    private void configureBottomNavigation() {
        boolean accessWriteData = Boolean.parseBoolean(getIntent().getStringExtra(Constants.ACCESS_DATA));
        viewModel.getPageConfiguration().observe(this, binding.navigationBar::pageConfiguration);

        binding.navigationBar.setOnItemSelectedListener(item -> {
            Fragment newFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_list_view:
                    newFragment = DataSetListFragment.newInstance(dataSetUid, accessWriteData);
                    break;
                case R.id.navigation_analytics:
                    presenter.trackDataSetAnalytics();
                    newFragment = GroupAnalyticsFragment.Companion.forDataSet(dataSetUid);
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (fragment == null ||(newFragment != null  && !fragment.getClass().toString().equals(newFragment.getClass().toString()))) {
                fragment = newFragment;
                transaction.replace(R.id.fragmentContainer, fragment).commit();
            }
            return true;
        });
        binding.navigationBar.selectItemAt(0);
        binding.fragmentContainer.setPadding(0, 0, 0, binding.navigationBar.isHidden() ? 0 : ExtensionsKt.getDp(56));

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init();
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
    }

    @Override
    protected void onPause() {
        presenter.setOpeningFilterToNone();
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void showHideFilter() {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        backDropActive = !backDropActive;
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);
        if (backDropActive) {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.filterLayout, ConstraintSet.BOTTOM, 50);
        } else {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }
        initSet.applyTo(binding.backdropLayout);

        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);
        ViewCompat.setElevation(binding.eventsLayout, backDropActive ? 20 : 0);
    }

    @Override
    public void clearFilters() {
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public void openOrgUnitTreeSelector() {
        new OUTreeFragment.Builder()
                .showAsDialog()
                .withPreselectedOrgUnits(FilterManager.getInstance().getOrgUnitUidsFilters())
                .onSelection(selectedOrgUnits -> {
                    presenter.setOrgUnitFilters((List<OrganisationUnit>) selectedOrgUnits);
                    return Unit.INSTANCE;
                })
                .build()
                .show(getSupportFragmentManager(), "OUTreeFragment");
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(this, datePeriods -> filterManager.addPeriod(datePeriods));
        } else {
            DateUtils.getInstance().showPeriodDialog(
                    this,
                    datePeriods -> filterManager.addPeriod(datePeriods),
                    true
            );
        }
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

    @Override
    public void setFilters(List<FilterItem> filterItems) {
        filtersAdapter.submitList(filterItems);
    }

    @Override
    public void hideFilters() {
        binding.filter.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        presenter.clearFilterIfDatasetConfig();
        super.onDestroy();
    }

    public void setProgress(boolean active) {
        binding.programProgress.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showGranularSync() {
        presenter.trackDataSetGranularSync();
        new SyncStatusDialog.Builder()
                .withContext(this, null)
                .withSyncContext(new SyncContext.DataSet(dataSetUid))
                .onDismissListener(hasChanged -> presenter.refreshList())
                .show("DATASET_SYNC");
    }
}
