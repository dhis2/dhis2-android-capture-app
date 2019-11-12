/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.processors.PublishProcessor;


public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailView {

    private ActivityDatasetDetailBinding binding;
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    private String dataSetUid;
    private Boolean accessDataWrite;

    @Inject
    DataSetDetailPresenter presenter;

    private static PublishProcessor<Integer> currentPage;
    DataSetDetailAdapter adapter;
    private FiltersAdapter filtersAdapter;
    private boolean backDropActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new DataSetDetailModule(this, getIntent().getStringExtra("DATASET_UID"))).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);

        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());

        dataSetUid = getIntent().getStringExtra("DATASET_UID");
        binding.setName(getIntent().getStringExtra(Constants.DATA_SET_NAME));
        accessDataWrite = Boolean.valueOf(getIntent().getStringExtra(Constants.ACCESS_DATA));
        binding.setPresenter(presenter);

        adapter = new DataSetDetailAdapter(presenter);

        filtersAdapter = new FiltersAdapter();

        binding.filterLayout.setAdapter(filtersAdapter);

        currentPage = PublishProcessor.create();
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
    public void setData(@NotNull List<? extends DataSetDetailModel> datasets) {
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
            updateFilters(FilterManager.getInstance().getTotalFilters());
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
            DateUtils.getInstance().showFromToSelector(this, FilterManager.getInstance()::addPeriod);
        } else {
            DateUtils.getInstance().showPeriodDialog(this, datePeriods -> {
                        FilterManager.getInstance().addPeriod(datePeriods);
                    },
                    true);
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
        bundle.putString(Constants.PERIOD_ID, dataSet.periodId());
        bundle.putString(Constants.PERIOD_TYPE, dataSet.periodType());
        bundle.putString(Constants.PERIOD_TYPE_DATE, dataSet.namePeriod());
        bundle.putString(Constants.CAT_COMB, dataSet.catOptionComboUid());
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        bundle.putBoolean(Constants.ACCESS_DATA, accessDataWrite);
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
                    if(hasChanged)
                        presenter.updateFilters();
                })
                .build();

        dialog.show(getSupportFragmentManager(), dialog.getDialogTag());
    }
}
