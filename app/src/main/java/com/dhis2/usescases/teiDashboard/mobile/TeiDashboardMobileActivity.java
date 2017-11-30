package com.dhis2.usescases.teiDashboard.mobile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityDashboardMobileBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.TeiDashboardModule;
import com.dhis2.usescases.teiDashboard.TeiDashboardPresenter;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    ActivityDashboardMobileBinding binding;
    @Inject
    TeiDashboardPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).getUserComponent().plus(new TeiDashboardModule()).inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);
        presenter.init(this,getIntent().getStringExtra("TEI_UID"));
        binding.teiPager.setAdapter(new DashboardPagerAdapter(getSupportFragmentManager(), false));
    }


    @Override
    public void setData(TrackedEntityInstance trackedEntityModel) {
        TEIDataFragment.getInstance().setTrackedEntity(trackedEntityModel);

    }
}
