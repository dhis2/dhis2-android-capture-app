package com.dhis2.usescases.enrollment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.R;
import com.dhis2.databinding.ActivityEnrollmentBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public class EnrollmentActivity extends ActivityGlobalAbstract implements EnrollmentContracts.View {

    ActivityEnrollmentBinding binding;

    @Inject
    EnrollmentPresenter presenter;

    //------------------------------------------
    //region LIFECYCLE
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_enrollment);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(null);
    }

    @Override
    protected void onDestroy() {
        presenter.deAttach();
        super.onDestroy();
    }

    //endregion
}
