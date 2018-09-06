package org.dhis2.usescases.teiDashboard.dashboardfragments;

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

import org.dhis2.R;
import org.dhis2.databinding.FragmentScheduleBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.adapters.ScheduleAdapter;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.List;

import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ADDNEW;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ENROLLMENT_UID;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_CREATION_TYPE;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.NEW_EVENT;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ORG_UNIT;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_UID;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.REFERRAL;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.SCHEDULENEW;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.TRACKED_ENTITY_INSTANCE;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class ScheduleFragment extends FragmentGlobalAbstract implements View.OnClickListener {

    FragmentScheduleBinding binding;

    static ScheduleFragment instance;
    private ScheduleAdapter adapter;
    TeiDashboardContracts.Presenter presenter;
    PublishProcessor<ScheduleAdapter.Filter> currentFilter;
    ActivityGlobalAbstract activity;

    public static ScheduleFragment getInstance() {
        if (instance == null)
            instance = new ScheduleFragment();
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ActivityGlobalAbstract) context;
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false);
        adapter = new ScheduleAdapter();
        binding.scheduleRecycler.setAdapter(adapter);
        binding.scheduleFilter.setOnClickListener(this);
        currentFilter = PublishProcessor.create();

        binding.fab.setOptionsClick(integer -> {
            if (integer == null)
                return;

            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, presenter.getDashBoardData().getCurrentEnrollment().program());
            bundle.putString(TRACKED_ENTITY_INSTANCE, presenter.getTeUid());
            bundle.putString(ORG_UNIT, presenter.getDashBoardData().getCurrentEnrollment().organisationUnit());
            bundle.putString(ENROLLMENT_UID, presenter.getDashBoardData().getCurrentEnrollment().organisationUnit());
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

//            startActivity(EventInitialActivity.class, bundle, false, false, null);

            startActivity(ProgramStageSelectionActivity.class, bundle, false, false, null);

        });

        presenter.subscribeToScheduleEvents(this);
        currentFilter.onNext(ScheduleAdapter.Filter.ALL);
        return binding.getRoot();
    }

    @Override
    public void onClick(View view) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_list);
        int color;
        ScheduleAdapter.Filter filter = adapter.filter();
        currentFilter.onNext(filter);
        switch (filter) {
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

    public FlowableProcessor<ScheduleAdapter.Filter> filterProcessor() {
        return currentFilter;
    }
}
