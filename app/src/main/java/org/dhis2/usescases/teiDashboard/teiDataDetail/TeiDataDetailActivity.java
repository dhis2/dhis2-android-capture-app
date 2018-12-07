package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.FormFragment;
import org.dhis2.data.forms.FormViewArguments;
import org.dhis2.databinding.ActivityTeidataDetailBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class TeiDataDetailActivity extends ActivityGlobalAbstract implements TeiDataDetailContracts.View {
    ActivityTeidataDetailBinding binding;

    @Inject
    TeiDataDetailContracts.Presenter presenter;

    private DashboardProgramModel dashboardProgramModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiDataDetailModule(getIntent().getStringExtra("ENROLLMENT_UID"))).inject(this);

        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_teidata_detail);
        binding.setPresenter(presenter);

        init(getIntent().getStringExtra("TEI_UID"), getIntent().getStringExtra("PROGRAM_UID"), getIntent().getStringExtra("ENROLLMENT_UID"));

        binding.fabActive.setOptionsClick(integer -> {
            if (integer == null)
                return;

            switch (integer) {
                case R.id.deactivate:
                    presenter.onDeactivate(dashboardProgramModel);
                    break;
                case R.id.complete:
                    presenter.onComplete(dashboardProgramModel);
                    break;
            }
        });

        binding.fabCompleted.setOptionsClick(integer -> {
            if (integer == null)
                return;

            switch (integer) {
                case R.id.reOpen:
                    presenter.onReOpen(dashboardProgramModel);
                    break;
            }
        });

        binding.fabCancelled.setOptionsClick(integer -> {
            if (integer == null)
                return;

            switch (integer) {
                case R.id.activate:
                    presenter.onActivate(dashboardProgramModel);
            }
        });
    }

    @Override
    public void init(String teiUid, String programUid, String enrollmentUid) {
        presenter.init(this, teiUid, programUid, enrollmentUid);
    }

    @Override
    public void setData(DashboardProgramModel program) {
        this.dashboardProgramModel = program;
        binding.setDashboardModel(program);
        binding.setProgram(program.getCurrentProgram());
        binding.setEnrollmentStatus(program.getCurrentEnrollment().enrollmentStatus());
        binding.executePendingBindings();

        supportStartPostponedEnterTransition();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dataFragment, FormFragment.newInstance(
                        FormViewArguments.createForEnrollment(program.getCurrentEnrollment().uid()), true,
                        false))
                .commit();

    }


    @Override
    public Consumer<EnrollmentStatus> handleStatus() {
        return enrollmentStatus -> {
            Bindings.setEnrolmentIcon(binding.programLock, enrollmentStatus);
            Bindings.setEnrolmentText(binding.programLockText, enrollmentStatus);
            binding.setEnrollmentStatus(enrollmentStatus);
            binding.executePendingBindings();
        };
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

}