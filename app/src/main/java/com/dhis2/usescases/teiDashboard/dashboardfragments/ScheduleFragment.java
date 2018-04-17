package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
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
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.ScheduleAdapter;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.List;

import io.reactivex.functions.Consumer;

import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ADDNEW;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_CREATION_TYPE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.NEW_EVENT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.REFERRAL;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.SCHEDULENEW;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.TRACKED_ENTITY_INSTANCE;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class ScheduleFragment extends FragmentGlobalAbstract implements View.OnClickListener {

    FragmentScheduleBinding binding;

    static ScheduleFragment instance;
    private ScheduleAdapter adapter;
    private static String programUid;
    TeiDashboardContracts.Presenter presenter;

    public static ScheduleFragment getInstance() {
        if (instance == null)
            instance = new ScheduleFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false);
        adapter = new ScheduleAdapter();
        binding.scheduleRecycler.setAdapter(new ScheduleAdapter());
        binding.scheduleFilter.setOnClickListener(this);

        binding.fab.setOptionsClick(integer -> {
            if (integer == null)
                return;

            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, programUid);
            bundle.putString(TRACKED_ENTITY_INSTANCE, presenter.getTeUid());
            bundle.putString("ORG_UNIT", presenter.getProgramUid());
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

            startActivity(EventInitialActivity.class, bundle, false, false, null);
        });

        presenter.subscribeToScheduleEvents(this);

        return binding.getRoot();
    }

   /* public void setData(String mProgramUid, DashboardProgramModel mprogram) {
        programUid = mProgramUid;
        program = mprogram;
        binding.scheduleRecycler.setAdapter(new ScheduleAdapter());
        binding.scheduleFilter.setOnClickListener(this);
    }*/

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

    public Consumer<List<EventModel>> swapEvents() {
        return noteModels -> {
            adapter.setScheduleEvents(noteModels);
        };
    }
}
