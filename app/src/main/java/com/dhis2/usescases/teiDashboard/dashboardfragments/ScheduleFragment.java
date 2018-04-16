package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentScheduleBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.adapters.ScheduleAdapter;

import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ADDNEW;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_CREATION_TYPE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.REFERRAL;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.SCHEDULENEW;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class ScheduleFragment extends FragmentGlobalAbstract implements View.OnClickListener {

    FragmentScheduleBinding binding;

    static ScheduleFragment instance;
    private static DashboardProgramModel program;
    private static String programUid;

    public static ScheduleFragment getInstance() {
        if (instance == null)
            instance = new ScheduleFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false);
        if (program!= null)
            binding.scheduleRecycler.setAdapter(new ScheduleAdapter(program.getProgramStages(), program.getEvents()));
        binding.scheduleFilter.setOnClickListener(this);
        binding.fab.setOptionsClick(integer -> {
            if (integer == null)
                return;

            // TODO CRIS: REDIRECT TO PROGRAM STAGE SELECTION

            Bundle bundle = new Bundle();
            bundle.putString("PROGRAM_UID", programUid);
            bundle.putString("ORG_UNIT", program.getOrgUnit().uid());

            switch (integer){
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
        return binding.getRoot();
    }

    public void setData(String mProgramUid, DashboardProgramModel mprogram) {
        programUid = mProgramUid;
        program = mprogram;
        binding.scheduleRecycler.setAdapter(new ScheduleAdapter(program.getProgramStages(), program.getEvents()));
        binding.scheduleFilter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_list);
        int color;
        switch (((ScheduleAdapter) binding.scheduleRecycler.getAdapter()).filter()) {
            case SCHEDULE:
                color = ContextCompat.getColor(view.getContext(), R.color.green_7ed);
                break;
            case OVERDUE:
                color = ContextCompat.getColor(view.getContext(), R.color.red_060);
                break;
            default:
                TypedValue typedValue = new TypedValue();
                TypedArray a = view.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
                color = a.getColor(0, 0);
                a.recycle();
                break;
        }
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        binding.scheduleFilter.setImageDrawable(drawable);
    }
}
