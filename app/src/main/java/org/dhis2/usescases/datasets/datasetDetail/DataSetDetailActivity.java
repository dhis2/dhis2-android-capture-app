package org.dhis2.usescases.datasets.datasetDetail;

import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetDetailBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.orgunitselector.OUTreeFragment;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.dhis2.utils.filters.FilterItem;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.granularsync.SyncStatusDialog;

import java.util.List;

import javax.inject.Inject;

import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment;


public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailView {

    private ActivityDatasetDetailBinding binding;
    private String dataSetUid;
    public DataSetDetailComponent dataSetDetailComponent;

    @Inject
    DataSetDetailPresenter presenter;

    @Inject
    FilterManager filterManager;

    @Inject
    FiltersAdapter filtersAdapter;

    @Inject
    NavigationPageConfigurator pageConfigurator;

    private boolean backDropActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataSetUid = getIntent().getStringExtra("DATASET_UID");
        dataSetDetailComponent = ((App) getApplicationContext()).userComponent().plus(new DataSetDetailModule(this, dataSetUid));
        dataSetDetailComponent.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);
        binding.setName(getIntent().getStringExtra(Constants.DATA_SET_NAME));
        boolean accessWriteData = Boolean.parseBoolean(getIntent().getStringExtra(Constants.ACCESS_DATA));
        binding.setPresenter(presenter);

        ViewExtensionsKt.clipWithRoundedCorners(binding.eventsLayout, ExtensionsKt.getDp(16));
        binding.filterLayout.setAdapter(filtersAdapter);
        binding.navigationBar.pageConfiguration(pageConfigurator);
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_list_view:
                    fragment = DataSetListFragment.newInstance(dataSetUid, accessWriteData);
                    break;
                case R.id.navigation_analytics:
                    fragment = GroupAnalyticsFragment.Companion.forDataSet(dataSetUid);
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (fragment != null) {
                transaction.replace(R.id.fragmentContainer, fragment).commit();
            }
            return true;
        });
        binding.navigationBar.selectItemAt(0);
        binding.fragmentContainer.setPadding(0,0,0, binding.navigationBar.isHidden()? 0 : ExtensionsKt.getDp(56));
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
        OUTreeFragment.Companion.newInstance(true).show(getSupportFragmentManager(), "OUTreeFragment");
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
}
