package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryActivity;
import com.dhis2.usescases.map.MapSelectorActivity;
import com.dhis2.utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;

/**
 * Created by Cristian on 01/03/2018.
 */

public class EventInitialPresenter implements EventInitialContract.Presenter {

    public static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 101;
    static private EventInitialContract.View view;
    private final EventInitialContract.Interactor interactor;
    public ProgramModel program;
    private FusedLocationProviderClient mFusedLocationClient;
    private String eventId;


    EventInitialPresenter(EventInitialContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(EventInitialContract.View mview, String programId, String eventId) {
        view = mview;
        this.eventId = eventId;
        interactor.init(view, programId, eventId);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.getContext());
    }

    @Override
    public void getProgramStage(String programStageUid) {
        interactor.getProgramStageWithId(programStageUid);
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.program = program;
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void createEvent(String programStageModel, Date date, String orgUnitUid, String catComboUid, String catOptionUid, String latitude, String longitude) {
        if (program != null) {
            interactor.createNewEvent(programStageModel, program.uid(), date, orgUnitUid, catComboUid, catOptionUid, latitude, longitude);
        }
    }

    @Override
    public void createEventPermanent(String trackedEntityInstanceUid, String programStageModel, Date date, String orgUnitUid, String catComboUid, String catOptionUid, String latitude, String longitude) {
        interactor.createNewEventPermanent(trackedEntityInstanceUid, programStageModel, program.uid(), date, orgUnitUid, catComboUid, catOptionUid, latitude, longitude);
    }

    @Override
    public void editEvent(String programStageModel, String eventUid, String date, String orgUnitUid, String catComboUid, String latitude, String longitude) {
        interactor.editEvent(programStageModel, eventUid, date, orgUnitUid, catComboUid, latitude, longitude);
    }

    @Override
    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {
        view.showDateDialog(listener);
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
    }

    @Override
    public void onLocationClick() {
        if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(view.getAbstractActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // TODO CRIS:  Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(view.getAbstractActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
            }
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null)
                view.setLocation(location.getLatitude(), location.getLongitude());
        });
    }

    @Override
    public void onLocation2Click() {
        Intent intent = new Intent(view.getContext(), MapSelectorActivity.class);
        view.getAbstractActivity().startActivityForResult(intent, Constants.RQ_MAP_LOCATION);
    }

    @Override
    public void getCatOption(String categoryOptionComboId) {
        interactor.getCatOption(categoryOptionComboId);
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
    public void filterOrgUnits(String date) {
        interactor.getFilteredOrgUnits(date);
    }

    @Override
    public void onDetach() {
        interactor.onDetach();
    }

    @Override
    public void goToSummary() {
        Bundle bundle = new Bundle();
        bundle.putString("event_id", eventId);
        bundle.putString("program_id", program.uid());
        view.startActivity(EventSummaryActivity.class, bundle, false, false, null);
    }

    @Override
    public void getSectionCompletion(@Nullable String sectionUid) {
        interactor.getSectionCompletion(sectionUid);
    }
}
