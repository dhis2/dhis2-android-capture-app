package com.dhis2.usescases.searchTrackEntity;

import android.util.Log;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.programDetail.TrackedEntityObject;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class SearchTEInteractor implements SearchTEContractsModule.Interactor {

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;
    private SearchTEContractsModule.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private List<OrganisationUnitModel> orgList;
    private List<ProgramModel> programModels;
    private ProgramModel selectedProgram;
    private String enrollmentDate;
    private String incidentDate;
    private List<String> filters;
    private List<TrackedEntityAttributeModel> attributeModelList;
    private String trackedEntityType;
    private TrackedEntityModel trackedEntity;

    private int currentPage = 0;

    public SearchTEInteractor(SearchRepository searchRepository, UserRepository userRepository, MetadataRepository metadataRepository) {
        this.searchRepository = searchRepository;
        this.userRepository = userRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(SearchTEContractsModule.View view, String trackedEntityType) {
        this.view = view;
        this.trackedEntityType = trackedEntityType;
        filters = new ArrayList<>();
        getTrackedEntityAttributes();
    }

    @Override
    public void getTrackedEntityAttributes() {

        compositeDisposable.add(metadataRepository.getTrackedEntity(trackedEntityType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> this.trackedEntity = data));


        compositeDisposable.add(searchRepository.programAttributes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            attributeModelList = new ArrayList<>();
                            attributeModelList.addAll(data);
                            view.setForm(data, selectedProgram);
                        },
                        Timber::d)
        );

        compositeDisposable.add(userRepository.myOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgsUnits -> {
                            orgList = new ArrayList<>();
                            orgList.addAll(orgsUnits);
                            call();
                        },
                        Timber::d)
        );

        compositeDisposable.add(searchRepository.programsWithRegistration(trackedEntityType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programModels -> {
                            this.programModels = programModels;
                            view.setPrograms(programModels);
                        },
                        Timber::d)
        );

    }

    @Override
    public void getProgramTrackedEntityAttributes() {
        if (selectedProgram != null)
            compositeDisposable.add(searchRepository.programAttributes(selectedProgram.uid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> {
                                view.setForm(data, selectedProgram);
                            },
                            Timber::d)
            );
    }

    @Override
    public Observable<List<OptionModel>> getOptions(String optionSetId) {

        return searchRepository.optionSet(optionSetId);
    }

    //Gets all trackedEntityInstances applaying queries (Should be changed when sdk is ready
    @Override
    public void filterTrackEntities(String filter) {

        String found = null;
        for (String string : filters) {
            if (filter.split(":")[0].equals(string.split(":")[0]))
                found = string;
        }

        if (found != null)
            filters.remove(found);

        if (filter.split(":").length == 3)
            filters.add(filter);

        call();

    }

    @Override
    public void addDateQuery(String filter) {
        if (filter.contains("Enroll"))
            enrollmentDate = filter.split("=")[1];
        else
            incidentDate = filter.split("=")[1];

        call();

    }

    @Override
    public void clear() {
        filters.clear();
        selectedProgram = null;
        enrollmentDate = null;
        incidentDate = null;

        getTrackedEntityAttributes();

        //Clears tei list
        Observable.just(new ArrayList<TrackedEntityInstanceModel>())
                .subscribe(view.swapListData(),
                        t -> Log.d("ERROR", t.getMessage()));
    }

    @Override
    public void clearFilter(String uid) {
        String filterToRemove = null;
        for (String filterUid : filters) {
            if (filterUid.contains(uid))
                filterToRemove = filterUid;
        }
        if (filterToRemove != null)
            filters.remove(filterToRemove);

        call();
    }

    @Override
    public void enroll() {

    }

    @Override
    public TrackedEntityModel getTrackedEntity() {
        return trackedEntity;
    }

    private void call() {
        String orgQuey = "";
        for (int i = 0; i < orgList.size(); i++) {
            orgQuey = orgQuey.concat(orgList.get(i).uid());
            if (i < orgList.size() - 1)
                orgQuey = orgQuey.concat(";");
        }

        compositeDisposable.add(teiObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.swapListData(),
                        throwable -> Log.d("ERROR", throwable.getMessage())));

    }

    private Observable<List<TrackedEntityInstanceModel>> teiObservable() {
        return Observable.defer(() -> searchRepository.trackedEntityInstances(trackedEntityType, selectedProgram.uid(), enrollmentDate, incidentDate, null));
    }


    @Override
    public void setProgram(ProgramModel programSelected) {
        if (programSelected != null) {
            for (ProgramModel programModel : programModels)
                if (programModel.uid().equals(programSelected.uid()))
                    this.selectedProgram = programSelected;
            getProgramTrackedEntityAttributes();
        } else {
            this.selectedProgram = null;
            getTrackedEntityAttributes();
        }
        currentPage = 0;
        call();

    }
}
