package com.dhis2.usescases.dataset.dataSetPeriod;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityDataSetPeriodBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.dataset.DataSet;

import javax.inject.Inject;

public class DataSetPeriodActivity extends ActivityGlobalAbstract implements DataSetPeriodContract.View {

    ActivityDataSetPeriodBinding binding;
    @Inject
    DataSetPeriodContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getApplicationContext()).userComponent().plus(new DataSetPeriodModule()).inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_set_period);
        binding.setPresenter(presenter);
        String dataSetId = getIntent().getStringExtra("DATASET_UID");
        presenter.init(this, dataSetId);
    }

    @Override
    public void setDataSet(DataSet dataSet) {
        binding.setName(dataSet.name());
    }
}
