package com.dhis2.usescases.teiDashboard.mobile;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.View;

import com.dhis2.R;
import com.dhis2.data.forms.FormActivity;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.databinding.ActivityDashboardMobileBinding;
import com.dhis2.usescases.qrCodes.QrActivity;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardActivity;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;
import com.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity;
import com.dhis2.utils.Constants;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class TeiDashboardMobileActivity extends TeiDashboardActivity implements TeiDashboardContracts.View {

    ActivityDashboardMobileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);

        binding.tabLayout.setupWithViewPager(binding.teiPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        binding.toolbarTitle.setLines(1);
        binding.toolbarTitle.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init(teiUid, programUid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onDettach();
    }

    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this, teiUid, programUid);
    }

    @Override
    public void setData(DashboardProgramModel program) {
        adapter = new DashboardPagerAdapter(getSupportFragmentManager(), program, getResources().getBoolean(R.bool.is_tablet));
        binding.teiPager.setAdapter(adapter);

        if (getResources().getBoolean(R.bool.is_tablet))
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tei_main_view, TEIDataFragment.getInstance())
                    .commit();

        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        String title = program.getAttributeBySortOrder(1) + " " + program.getAttributeBySortOrder(2) + " - " + program.getCurrentProgram().displayShortName();
        binding.setTitle(title);

        binding.tabLayout.setVisibility(View.VISIBLE);
        binding.executePendingBindings();
        this.programModel = program;
        TEIDataFragment.getInstance().setData(programModel);
        RelationshipFragment.getInstance().setData(program);
        binding.teiPager.setOffscreenPageLimit(6);
    }

    @Override
    public void setDataWithOutProgram(DashboardProgramModel program) {
        adapter = new DashboardPagerAdapter(getSupportFragmentManager(), program, getResources().getBoolean(R.bool.is_tablet));
        binding.teiPager.setAdapter(adapter);

        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        String title = program.getAttributeBySortOrder(1) + " " + program.getAttributeBySortOrder(2);
        binding.setTitle(title);
        binding.tabLayout.setVisibility(View.GONE);
        binding.executePendingBindings();
        this.programModel = program;
        TEIDataFragment.getInstance().setData(programModel);
        binding.teiPager.setOffscreenPageLimit(6);

    }

    @Override
    public DashboardPagerAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void showQR() {
        Intent intent = new Intent(TeiDashboardMobileActivity.this, QrActivity.class);
        intent.putExtra("TEI_UID", teiUid);
        startActivity(intent);
    }

    @Override
    public void goToEnrollmentList(Bundle extras) {
        Intent intent = new Intent(this, TeiProgramListActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.RQ_ENROLLMENTS);
    }

    @Override
    public String getToolbarTitle() {
        return binding.toolbarTitle.getText().toString();
    }

    public TeiDashboardContracts.Presenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RQ_ENROLLMENTS && resultCode == RESULT_OK) {
            if (data.hasExtra("GO_TO_ENROLLMENT")) {
                FormViewArguments formViewArguments = FormViewArguments.createForEnrollment(data.getStringExtra("GO_TO_ENROLLMENT"));
                startActivity(FormActivity.create(this, formViewArguments, true));
            }

            if (data.hasExtra("CHANGE_PROGRAM")) {
                programUid = data.getStringExtra("CHANGE_PROGRAM");
                init(teiUid, data.getStringExtra("CHANGE_PROGRAM"));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}