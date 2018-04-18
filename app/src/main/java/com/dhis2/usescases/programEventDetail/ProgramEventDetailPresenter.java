package com.dhis2.usescases.programEventDetail;

import android.os.Bundle;

import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
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
    public String programId;

    ProgramEventDetailPresenter(ProgramEventDetailContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(ProgramEventDetailContract.View mview, String programId, Period period) {
        view = mview;
        this.programId=programId;

        interactor.init(view, programId, period);
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
        interactor.getEvents(programId, fromDate, toDate);
    }

    @Override
    public void getProgramEventsWithDates(List<Date> dates, Period period) {
        interactor.getProgramEventsWithDates(programId, dates, period);
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
        bundle.putString("PROGRAM_UID", programId);
        bundle.putString("EVENT_UID", eventId);
        bundle.putBoolean("NEW_EVENT", false);
        view.startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public ProgramModel getCurrentProgram() {
        return program;
    }

    public void addEvent() {
        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", programId);
        bundle.putBoolean("NEW_EVENT", true);
        view.startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }
}
