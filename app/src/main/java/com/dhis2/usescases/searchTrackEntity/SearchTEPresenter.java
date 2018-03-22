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

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.forms.FormActivity;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private final MetadataRepository metadataRepository;
    private final SearchRepository searchRepository;
    private final UserRepository userRepository;
    private SearchTEContractsModule.View view;

    private LocationManager locationManager;
    private ProgramModel selectedProgram;

    private CompositeDisposable compositeDisposable;
    private TrackedEntityModel trackedEntity;
    private String enrollmentDate;
    private String incidentDate;
    private List<ProgramModel> programModels;
    private HashMap<String, String> queryData;

    public SearchTEPresenter(SearchRepository searchRepository, UserRepository userRepository, MetadataRepository metadataRepository) {
        Bindings.setMetadataRepository(metadataRepository);
        this.userRepository = userRepository;
        this.metadataRepository = metadataRepository;
        this.searchRepository = searchRepository;
        compositeDisposable = new CompositeDisposable();
        queryData = new HashMap<>();
    }

    //-----------------------------------
    //region LIFECYCLE

    @Override
    public void init(SearchTEContractsModule.View view, String trackedEntityType) {
        this.view = view;
        locationManager = (LocationManager) view.getAbstracContext().getSystemService(Context.LOCATION_SERVICE);

        compositeDisposable.add(
                metadataRepository.getTrackedEntity(trackedEntityType)
                        .switchMap(trackedEntity ->
                        {
                            this.trackedEntity = trackedEntity;
                            getTrakedEntities();
                            return searchRepository.programsWithRegistration(trackedEntityType);
                        })
                        .switchMap(programModels -> {
                            this.programModels = programModels;
                            view.setPrograms(programModels);
                            return searchRepository.programAttributes();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.setForm(data, selectedProgram),
                                Timber::d)
        );

        compositeDisposable.add(view.rowActions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (!data.value.isEmpty())
                        queryData.put(data.uid, data.value);
                    else
                        queryData.remove(data.uid);
                    getTrakedEntities();
                })
        );

        compositeDisposable.add(view.rowActionss()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (!data.value().isEmpty())
                        queryData.put(data.id(), data.value());
                    else
                        queryData.remove(data.id());
                    getTrakedEntities();
                })
        );

    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
    }
    //endregion

    //------------------------------------------
    //region DATA

    private void getTrakedEntities() {
        compositeDisposable.add(searchRepository.trackedEntityInstances(trackedEntity.uid(),
                selectedProgram != null ? selectedProgram.uid() : null, enrollmentDate, incidentDate, queryData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.swapListData(), Timber::d)
        );
    }

    private void getTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setForm(data, selectedProgram),
                        Timber::d)
        );
    }

    public void getProgramTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes(selectedProgram.uid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setForm(data, selectedProgram),
                        Timber::d)
        );
    }

    @Override
    public TrackedEntityModel getTrackedEntityName() {
        return trackedEntity;
    }

    @Override
    public ProgramModel getProgramModel() {
        return selectedProgram;
    }

    @Override
    public List<ProgramModel> getProgramList() {
        return programModels;
    }


    @Override
    public Observable<List<OptionModel>> getOptions(String optionSetId) {

        return searchRepository.optionSet(optionSetId);
    }

    //endregion


    @Override
    public void query(String filter, boolean isAttribute) {

    }

    @Override
    public void setProgram(ProgramModel programSelected) {
        selectedProgram = programSelected;
        view.clearList();

        getTrakedEntities();
        if (selectedProgram == null)
            getTrackedEntityAttributes();
        else
            getProgramTrackedEntityAttributes();
    }

    @Override
    public void onClearClick() {
        setProgram(null);
    }

    @Override
    public void clearFilter(String uid) {
    }

    //endregion

    @Override
    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {
        view.showDateDialog(listener);
    }

    @Override
    public void onBackClick() {
        view.back();
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
    public void onEnrollClick(View view) {
        if (view.isEnabled()) {
            enroll(selectedProgram.uid());
        } else
            this.view.displayMessage("Select a program to enable enrolling");
    }

    @Override
    public void enroll(String programUid) {
        //TODO: NEED TO SELECT ORG UNIT AND THEN SAVE AND CREATE ENROLLMENT BEFORE DOING THIS: FOR DEBUG USE ORG UNIT DiszpKrYNg8

        compositeDisposable.add(
                searchRepository.saveToEnroll(trackedEntity.uid(), "DiszpKrYNg8", programUid)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(enrollmentUid -> {
                                    FormViewArguments formViewArguments = FormViewArguments.createForEnrollment(enrollmentUid);
                                    this.view.getContext().startActivity(FormActivity.create(this.view.getAbstractActivity(), formViewArguments));
                                },
                                Timber::d)
        );


    }

    @Override
    public void onTEIClick(String TEIuid) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", TEIuid);
        bundle.putString("PROGRAM_UID", selectedProgram != null ? selectedProgram.uid() : null);
       /* if (view.getContext().getResources().getBoolean(R.bool.is_tablet))
            view.startActivity(TeiDashboardTabletActivity.class, bundle, false, false, null);
        else*/
        view.startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }


}
