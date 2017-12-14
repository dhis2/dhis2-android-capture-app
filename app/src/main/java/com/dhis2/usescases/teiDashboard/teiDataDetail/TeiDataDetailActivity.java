package com.dhis2.usescases.teiDashboard.teiDataDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.ViewTreeObserver;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityTeidataDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import javax.inject.Inject;

public class TeiDataDetailActivity extends ActivityGlobalAbstract implements TeiDataDetailContracts.View {
    ActivityTeidataDetailBinding binding;

    @Inject
    TeiDataDetailPresenter presenter;
    private boolean isEditable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).getUserComponent().plus(new TeiDataDetailModule()).inject(this);
        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_teidata_detail);
        binding.setPresenter(presenter);
        init(getIntent().getStringExtra("TEI_UID"), getIntent().getStringExtra("PROGRAM_UID"));
        isEditable = getIntent().getBooleanExtra("IS_EDITABLE", false);

    }

    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this, teiUid, programUid);
    }

    @Override
    public void setData(TrackedEntityInstance trackedEntityInstance, DashboardProgramModel program) {
        binding.setDashboardModel(program);
        binding.setTrackEntity(trackedEntityInstance);
        binding.setProgram(program.getProgram());
        binding.executePendingBindings();
        supportStartPostponedEnterTransition();

    }
}