package org.dhis2.usescases.datasets.datasetDetail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityDatasetDetailBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.orgunitselector.OUTreeActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

import java.util.List;

import javax.inject.Inject;


public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailView {

    private ActivityDatasetDetailBinding binding;
    private String dataSetUid;
    private Boolean accessWriteData;

    @Inject
    DataSetDetailPresenter presenter;

    @Inject
    FilterManager filterManager;

    DataSetDetailAdapter adapter;
    private FiltersAdapter filtersAdapter;
    private boolean backDropActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataSetUid = getIntent().getStringExtra("DATASET_UID");
        ((App) getApplicationContext()).userComponent().plus(new DataSetDetailModule(this, dataSetUid)).inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);
        binding.setName(getIntent().getStringExtra(Constants.DATA_SET_NAME));
        accessWriteData = Boolean.valueOf(getIntent().getStringExtra(Constants.ACCESS_DATA));
        binding.setPresenter(presenter);

        adapter = new DataSetDetailAdapter(presenter);

        filtersAdapter = new FiltersAdapter(FiltersAdapter.ProgramType.DATASET);

        binding.filterLayout.setAdapter(filtersAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init();
        binding.addDatasetButton.setEnabled(true);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setData(List<DataSetDetailModel> datasets) {
        binding.programProgress.setVisibility(View.GONE);
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
            binding.recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        }
        if(datasets.size() == 0){
            binding.emptyTeis.setVisibility(View.VISIBLE);
            binding.recycler.setVisibility(View.GONE);
        } else {
            binding.emptyTeis.setVisibility(View.GONE);
            binding.recycler.setVisibility(View.VISIBLE);
            adapter.setDataSets(datasets);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FilterManager.OU_TREE && resultCode == Activity.RESULT_OK) {
            filtersAdapter.notifyDataSetChanged();
            updateFilters(filterManager.getTotalFilters());
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        }
        else {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }
        initSet.applyTo(binding.backdropLayout);

        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);
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
        Intent ouTreeIntent = new Intent(this, OUTreeActivity.class);
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().showFromToSelector(this, datePeriods -> filterManager.addPeriod(datePeriods));
        } else {
            DateUtils.getInstance().showPeriodDialog(
                    this,
                    datePeriods -> filterManager.addPeriod(datePeriods),
                    true
            );
        }
    }

    @Override
    public void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos) {
        filtersAdapter.addCatOptCombFilter(categoryOptionCombos);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setWritePermission(Boolean canWrite) {
        binding.addDatasetButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
    }

    @Override
    public void startNewDataSet() {
        binding.addDatasetButton.setEnabled(false);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        startActivity(DataSetInitialActivity.class,bundle,false,false,null);
    }

    @Override
    public void openDataSet(DataSetDetailModel dataSet) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ORG_UNIT, dataSet.orgUnitUid());
        bundle.putString(Constants.ORG_UNIT_NAME, dataSet.nameOrgUnit());
        bundle.putString(Constants.PERIOD_TYPE_DATE, dataSet.namePeriod());
        bundle.putString(Constants.PERIOD_TYPE, dataSet.periodType());
        bundle.putString(Constants.PERIOD_ID, dataSet.periodId());
        bundle.putString(Constants.CAT_COMB, dataSet.catOptionComboUid());
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putBoolean(Constants.ACCESS_DATA, accessWriteData);
        startActivity(DataSetTableActivity.class, bundle, false, false, null);

    }

    @Override
    public void showSyncDialog(DataSetDetailModel dataSet) {
        SyncStatusDialog dialog = new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.DATA_VALUES)
                .setUid(dataSetUid)
                .setOrgUnit(dataSet.orgUnitUid())
                .setAttributeOptionCombo(dataSet.catOptionComboUid())
                .setPeriodId(dataSet.periodId())
                .onDismissListener(hasChanged -> {
                    if (hasChanged) {
                        presenter.updateFilters();
                    }
                }).build();

        dialog.show(getSupportFragmentManager(), dialog.getDialogTag());
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
