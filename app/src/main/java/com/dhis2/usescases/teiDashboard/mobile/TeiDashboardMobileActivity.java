package com.dhis2.usescases.teiDashboard.mobile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.R;
import com.dhis2.databinding.ActivityDashboardMobileBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardPagerAdapter;

import dagger.android.AndroidInjection;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract {

    ActivityDashboardMobileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.teiPager.setAdapter(new DashboardPagerAdapter(getSupportFragmentManager(), false));
    }


}
