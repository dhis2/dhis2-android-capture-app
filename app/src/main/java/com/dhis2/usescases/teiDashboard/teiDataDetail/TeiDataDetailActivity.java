package com.dhis2.usescases.teiDashboard.teiDataDetail;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.FormFragment;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.databinding.ActivityTeidataDetailBinding;
import com.dhis2.databinding.FormEditTextTeiDataBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.google.android.flexbox.FlexboxLayout;

import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import javax.inject.Inject;

public class TeiDataDetailActivity extends ActivityGlobalAbstract implements TeiDataDetailContracts.View {
    ActivityTeidataDetailBinding binding;

    @Inject
    TeiDataDetailContracts.Presenter presenter;

    private ObservableBoolean isEditable = new ObservableBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiDataDetailModule(getIntent().getStringExtra("ENROLLMENT_UID"))).inject(this);

        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_teidata_detail);
        binding.setPresenter(presenter);

        init(getIntent().getStringExtra("TEI_UID"), getIntent().getStringExtra("PROGRAM_UID"));
    }

    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this, teiUid, programUid);
    }

    @Override
    public void setData(DashboardProgramModel program) {
        binding.setDashboardModel(program);
        binding.setProgram(program.getCurrentProgram());
        binding.executePendingBindings();

        supportStartPostponedEnterTransition();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dataFragment, FormFragment.newInstance(
                        FormViewArguments.createForEnrollment(program.getCurrentEnrollment().uid()), true,
                        false))
                .commit();

    }


    @Override
    public void setDataEditable() {
        isEditable.set(!isEditable.get());
        binding.dataLayout.invalidate();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

}