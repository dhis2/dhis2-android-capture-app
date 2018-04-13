package com.dhis2.usescases.searchTrackEntity;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.forms.FormActivity;
import com.dhis2.data.forms.FormViewArguments;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.HashMap;
import java.util.List;

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
    private TrackedEntityTypeModel trackedEntity;
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap(trackedEntity ->
                        {
                            this.trackedEntity = trackedEntity;
//                            getTrakedEntities(); Should not look for all trackedEntities
                            return searchRepository.programsWithRegistration(trackedEntityType);
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(programModels -> {
                            this.programModels = programModels;
                            view.setPrograms(programModels);
                            return searchRepository.programAttributes();
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.setForm(data, selectedProgram),
                                Timber::d)
        );

      /*  compositeDisposable.add(view.rowActions()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            if (!data.value.isEmpty())
                                queryData.put(data.uid, data.value);
                            else
                                queryData.remove(data.uid);
                            getTrakedEntities();
                        },
                        Timber::d)
        );*/

        compositeDisposable.add(view.rowActionss()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            if (!data.value().isEmpty())
                                queryData.put(data.id(), data.value());
                            else
                                queryData.remove(data.id());
                            getTrakedEntities();
                        },
                        Timber::d)
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
        if (!queryData.isEmpty())
            compositeDisposable.add(searchRepository.trackedEntityInstances(trackedEntity.uid(),
                    selectedProgram != null ? selectedProgram.uid() : null, enrollmentDate, incidentDate, queryData)
//                    .subscribeOn(AndroidSchedulers.mainThread())
//                    .doOnSubscribe(data->view.getProgress().setVisibility(View.VISIBLE))
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
    public TrackedEntityTypeModel getTrackedEntityName() {
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

    //endregion


    @Override
    public void query(String filter, boolean isAttribute) {

    }

    @Override
    public void setProgram(ProgramModel programSelected) {
        selectedProgram = programSelected;
        view.clearList(programSelected == null ? null : programSelected.uid());

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


    //endregion

    @Override
    public void onBackClick() {
        view.back();
    }


    @Override
    public void onEnrollClick(View view) {
        if (view.isEnabled()) {
            enroll(selectedProgram.uid(), null);
        } else
            this.view.displayMessage("Select a program to enable enrolling");
    }

    @Override
    public void enroll(String programUid, String uid) {
        //TODO: NEED TO SELECT ORG UNIT AND THEN SAVE AND CREATE ENROLLMENT BEFORE DOING THIS: FOR DEBUG USE ORG UNIT DiszpKrYNg8

        compositeDisposable.add(
                searchRepository.saveToEnroll(trackedEntity.uid(), "DiszpKrYNg8", programUid, uid, queryData)
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
        view.startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }


}
