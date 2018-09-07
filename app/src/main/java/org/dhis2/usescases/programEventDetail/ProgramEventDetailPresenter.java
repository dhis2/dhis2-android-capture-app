package org.dhis2.usescases.programEventDetail;

import android.os.Bundle;

import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

import static org.dhis2.utils.Constants.NEW_EVENT;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;


/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    private ProgramEventDetailContract.View view;
    private final ProgramEventDetailContract.Interactor interactor;
    public ProgramModel program;
    public String programId;

    ProgramEventDetailPresenter(ProgramEventDetailContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(ProgramEventDetailContract.View mview, String programId, Period period) {
        view = mview;
        this.programId = programId;

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
    public void getProgramEventsWithDates(List<Date> dates, Period period, String orgUnitQuery) {
        interactor.getProgramEventsWithDates(programId, dates, period, orgUnitQuery);
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return interactor.getOrgUnits();
    }

    @Override
    public void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery) {
        interactor.updateFilters(categoryOptionComboModel, orgUnitQuery);
    }

    @Override
    public void clearCatComboFilters(String orgUnitQuery) {
        interactor.updateFilters(null, orgUnitQuery);
    }

    @Override
    public void onEventClick(String eventId, String orgUnit) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putString(Constants.EVENT_UID, eventId);
        bundle.putString(ORG_UNIT, orgUnit);
        bundle.putBoolean(NEW_EVENT, false);
        view.startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public Observable<List<TrackedEntityDataValueModel>> getEventDataValue(EventModel event) {
        return interactor.getEventDataValue(event);
    }

    @Override
    public Observable<List<String>> getEventDataValueNew(EventModel event) {
        return interactor.getEventDataValueNew(event);
    }

    @Override
    public ProgramModel getCurrentProgram() {
        return program;
    }

    public void addEvent() {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putBoolean(NEW_EVENT, true);
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

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }
}
