package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentTeiDataBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardProgramAdapter;
import com.dhis2.usescases.teiDashboard.adapters.EventAdapter;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.utils.CustomViews.CustomDialog;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.DialogClickListener;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ADDNEW;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_CREATION_TYPE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.NEW_EVENT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.REFERRAL;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.SCHEDULENEW;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.TRACKED_ENTITY_INSTANCE;

/**
 * -Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract implements DialogClickListener {

    private static final int REQ_DETAILS = 1001;
    private static final int REQ_EVENT = 2001;

    FragmentTeiDataBinding binding;

    static TEIDataFragment instance;
    TeiDashboardContracts.Presenter presenter;
    private DashboardProgramModel dashboardProgramModel;
    private boolean mIsBackVisible;

    private EventAdapter adapter;
    private List<EventModel> events = new ArrayList<>();

    static public TEIDataFragment getInstance() {
        if (instance == null)
            instance = new TEIDataFragment();

        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        binding.setPresenter(presenter);

        binding.fab.setOptionsClick(integer -> {
            if (integer == null)
                return;

            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, presenter.getDashBoardData().getCurrentEnrollment().program());
            bundle.putString(TRACKED_ENTITY_INSTANCE, presenter.getTeUid());
            bundle.putString("ORG_UNIT", presenter.getDashBoardData().getCurrentEnrollment().organisationUnit());
            bundle.putString("ENROLLMENT_UID", presenter.getDashBoardData().getCurrentEnrollment().organisationUnit());
            bundle.putBoolean(NEW_EVENT, true);

            switch (integer) {
                case R.id.referral:
                    bundle.putString(EVENT_CREATION_TYPE, REFERRAL);
                    break;
                case R.id.addnew:
                    bundle.putString(EVENT_CREATION_TYPE, ADDNEW);
                    break;
                case R.id.schedulenew:
                    bundle.putString(EVENT_CREATION_TYPE, SCHEDULENEW);
                    break;
            }

            startActivity(ProgramStageSelectionActivity.class, bundle, false, false, null);

        });
        binding.cardBack.cardBack.setAlpha(0f);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setData(dashboardProgramModel);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter = ((TeiDashboardMobileActivity) getActivity()).getPresenter();
        binding.setPresenter(presenter);
        setData(dashboardProgramModel);
        presenter.getTEIEvents(this);
    }

    public void setData(DashboardProgramModel nprogram) {
        this.dashboardProgramModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            this.events = new ArrayList<>();
            adapter = new EventAdapter(presenter, nprogram.getProgramStages(), events);
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(adapter);
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(nprogram.getCurrentEnrollment());
            binding.setProgram(nprogram.getCurrentProgram());
            binding.setDashboardModel(nprogram);
        } else if (nprogram != null) {
            binding.teiRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false));
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));

            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(null);
            binding.setProgram(null);
            binding.setDashboardModel(nprogram);
        }

        binding.executePendingBindings();

    }

    public static int getDetailsRequestCode() {
        return REQ_DETAILS;
    }

    public static int getEventRequestCode() {
        return REQ_EVENT;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DETAILS) {
            if (resultCode == RESULT_OK) {
                presenter.getData();
            }
        }
        if (requestCode == REQ_EVENT) {
            if (resultCode == RESULT_OK) {
                presenter.getTEIEvents(this);
                presenter.areEventsCompleted(this);
            }
        }
    }

    public Consumer<List<EventModel>> setEvents() {
        return events -> {
            adapter.swapItems(events);
            for (EventModel event : events) {
                if (event.eventDate().after(DateUtils.getInstance().getToday())) {
                    binding.teiRecycler.scrollToPosition(events.indexOf(event));
                }
            }
        };
    }


    public Consumer<Single<Boolean>> areEventsCompleted() {
        return eventsCompleted -> {
            if (eventsCompleted.blockingGet()) {
                CustomDialog dialog = new CustomDialog(
                        getContext(),
                        "Events Completed",
                        "All events in this program are completed. Would you like to close the program as well?",
                        "Ok",
                        "Cancel",
                        this);
                dialog.show();
            }

        };
    }

    public Consumer<EnrollmentStatus> enrollmentCompleted() {
        return enrollmentStatus -> {
            if (enrollmentStatus == EnrollmentStatus.COMPLETED)
                presenter.getData();
        };
    }

    @Override
    public void onPositive() {
        presenter.completeEnrollment(this);
    }

    @Override
    public void onNegative() {

    }

    public void flipCard(Bitmap bitmap) {
        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        binding.cardBack.qrImage.setImageBitmap(bitmap);
        binding.cardFront.cardFront.setCameraDistance(scale);
        binding.cardBack.cardBack.setCameraDistance(scale);
        AnimatorSet mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.flip_out_animation);
        AnimatorSet mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.flip_in_animation);
        if (!mIsBackVisible) {
            mSetRightOut.setTarget(binding.cardFront.cardFront);
            mSetLeftIn.setTarget(binding.cardBack.cardBack);
            mSetRightOut.start();
            mSetLeftIn.start();
            mIsBackVisible = true;
        } else {
            mSetRightOut.setTarget(binding.cardBack.cardBack);
            mSetLeftIn.setTarget(binding.cardFront.cardFront);
            mSetRightOut.start();
            mSetLeftIn.start();
            mIsBackVisible = false;
        }
    }
}