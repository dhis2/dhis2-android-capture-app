package com.dhis2.usescases.programEventDetail;

import android.os.Bundle;

import com.dhis2.R;
import com.dhis2.usescases.eventInitial.EventInitialActivity;
import com.dhis2.usescases.eventInitial.tablet.EventInitialTabletActivity;
import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;
import java.util.List;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    static private ProgramEventDetailContract.View view;
    private final ProgramEventDetailContract.Interactor interactor;
    public ProgramModel program;

    ProgramEventDetailPresenter(ProgramEventDetailContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(ProgramEventDetailContract.View mview, String programId) {
        view = mview;
        interactor.init(view, programId);
    }

    @Override
    public void onTimeButtonClick() {
        view.showTimeUnitPicker();
    }

    @Override
    public void onDateRangeButtonClick() {
        view.showRageDatePicker();
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.program = program;
    }

    @Override
    public void getEvents(Date fromDate, Date toDate) {
        interactor.getEvents(program.uid(), fromDate, toDate);
    }

    @Override
    public void getProgramEventsWithDates(List<Date> dates, Period period) {
        interactor.getProgramEventsWithDates(program.uid(), dates, period);
    }

    @Override
    public void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel) {
        interactor.updateFilters(categoryOptionComboModel);
    }

    @Override
    public void clearCatComboFilters() {
        interactor.updateFilters(null);
    }

    @Override
    public void onEventClick(String eventId) {
        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", program.uid());
        bundle.putString("EVENT_UID", eventId);
        bundle.putBoolean("NEW_EVENT", false);
        if (view.getContext().getResources().getBoolean(R.bool.is_tablet)) {
            view.startActivity(EventInitialTabletActivity.class, bundle, false, false, null);
        }
        else{
            view.startActivity(EventInitialActivity.class, bundle, false, false, null);
        }
    }

    @Override
    public ProgramModel getCurrentProgram() {
        return program;
    }

    public void addEvent() {
        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", program.uid());
        bundle.putBoolean("NEW_EVENT", true);
        if (view.getContext().getResources().getBoolean(R.bool.is_tablet)) {
            view.startActivity(EventInitialTabletActivity.class, bundle, false, false, null);
        }
        else{
            view.startActivity(EventInitialActivity.class, bundle, false, false, null);
        }
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }

    @Override
    public void filterOrgUnits(Date date) {
        interactor.getOrgUnits(date);
    }
}
