package org.dhis2.usescases.datasets.datasetInitial;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetInitialBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

public class DataSetInitialActivity extends ActivityGlobalAbstract {

    private ActivityDatasetInitialBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);
    }
}
