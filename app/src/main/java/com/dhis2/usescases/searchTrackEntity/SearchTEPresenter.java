package com.dhis2.usescases.searchTrackEntity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.dhis2.R;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.usescases.teiDashboard.tablet.TeiDashboardTabletActivity;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private SearchTEContractsModule.View view;
    @Inject
    SearchTEContractsModule.Interactor interactor;

    private LocationManager locationManager;
    private String selectedProgram;

    @Inject
    SearchTEPresenter() {
    }

    @Override
    public void init(SearchTEContractsModule.View view, String trackedEntityType) {
        this.view = view;
        interactor.init(view, trackedEntityType);
        locationManager = (LocationManager) view.getAbstracContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {
        view.showDateDialog(listener);
    }

    @Override
    public Observable<List<OptionModel>> getOptions(String optionSetId) {
        return interactor.getOptions(optionSetId);
    }

    @Override
    public void query(String filter, boolean isAttribute) {
        if (isAttribute)
            interactor.filterTrackEntities(filter);
        else
            interactor.addDateQuery(filter);
    }

    @Override
    public void setProgram(ProgramModel programSelected) {
        view.clearList();
        interactor.setProgram(programSelected);
        selectedProgram = programSelected.uid();
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onClearClick() {
        interactor.clear();
    }

    @Override
    public void requestCoordinates(LocationListener locationListener) {
        if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(view.getAbstracContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null)
                locationListener.onLocationChanged(lastLocation);
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
            }
        }
    }

    @Override
    public void clearFilter(String uid) {
        interactor.clearFilter(uid);
    }

    @Override
    public void onEnrollClick(View view) {
        if (view.isEnabled())
            interactor.enroll();
        else
            this.view.displayMessage("Select a program to enable enrolling");
    }

    @Override
    public void onTEIClick(String TEIuid) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", TEIuid);
        bundle.putString("PROGRAM_UID", selectedProgram);
        if (view.getContext().getResources().getBoolean(R.bool.is_tablet))
            view.startActivity(TeiDashboardTabletActivity.class, bundle, false, false, null);
        else
            view.startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);


    }

    public TrackedEntityModel getTrackedEntityName() {
        return interactor.getTrackedEntity();
    }
}
