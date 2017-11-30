package com.dhis2.usescases.teiDashboard.tablet;

import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityDashboardTabletBinding;
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

public class TeiDashboardTabletActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    ActivityDashboardTabletBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_tablet);
//        binding.setPresenter(new TeiDashboardPresenter());
        binding.teiPager.setAdapter(new DashboardPagerAdapter(getSupportFragmentManager(), true));
        getSupportFragmentManager().beginTransaction().add(R.id.tei_main_view, TEIDataFragment.getInstance()).commit();

    }

    @Override
    public void setData(TrackedEntityInstance trackedEntityModel) {
        TEIDataFragment.getInstance().setTrackedEntity(trackedEntityModel);
    }
}
