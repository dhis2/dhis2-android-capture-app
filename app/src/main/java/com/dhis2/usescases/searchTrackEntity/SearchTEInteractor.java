package com.dhis2.usescases.searchTrackEntity;

import android.util.Log;

import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.programDetail.TrackedEntityObject;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class SearchTEInteractor implements SearchTEContractsModule.Interactor {
    final String ouMode = "DESCENDANTS";

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;
    private SearchTEContractsModule.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private D2 d2;

    private List<OrganisationUnitModel> orgList;
    private List<ProgramModel> programModels;
    private ProgramModel selectedProgram;
    private String enrollmentDate;
    private String incidentDate;
    private List<String> filters;
    private List<TrackedEntityAttributeModel> attributeModelList;


    @Inject
    public SearchTEInteractor(D2 d2, SearchRepository searchRepository, UserRepository userRepository) {
        this.searchRepository = searchRepository;
        this.userRepository = userRepository;
        this.d2 = d2;
    }

    @Override
    public void init(SearchTEContractsModule.View view) {
        this.view = view;
        getTrackedEntityAttributes();
        filters = new ArrayList<>();
    }

    @Override
    public void getTrackedEntityAttributes() {
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

        compositeDisposable.add(searchRepository.programsWithRegistration()
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

        view.swapData(null, null, null);
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

    private void call() {
        String orgQuey = "";
        for (int i = 0; i < orgList.size(); i++) {
            orgQuey = orgQuey.concat(orgList.get(i).uid());
            if (i < orgList.size() - 1)
                orgQuey = orgQuey.concat(";");
        }

        d2.retrofit().create(TrackedEntityInstanceService.class)
                .trackEntityInstances(
                        orgQuey,
                        ouMode,
                        selectedProgram != null ? selectedProgram.uid() : null,
                        true,
                        enrollmentDate,
                        incidentDate,
                        filters,
                        "trackedEntityInstance,attributes[*],enrollments[enrollment,trackedEntity,orgUnit,program,trackedEntityInstance,incidentDate]")
                .enqueue(new Callback<TrackedEntityObject>() {
                    @Override
                    public void onResponse(Call<TrackedEntityObject> call, Response<TrackedEntityObject> response) {
                        view.swapData(response.body(), attributeModelList, programModels); //TODO: Send attributeList to order data in recycler and program list
                    }

                    @Override
                    public void onFailure(Call<TrackedEntityObject> call, Throwable t) {
                        Log.d("ONFAILURE", "onFailure: " + t.getMessage());
                    }
                });
    }


    @Override
    public void setProgram(ProgramModel programSelected) {
        if (programSelected != null) {
            for (ProgramModel programModel : programModels)
                if (programModel.uid() == programSelected.uid())
                    this.selectedProgram = programSelected;
            getProgramTrackedEntityAttributes();
        } else {
            this.selectedProgram = null;
            getTrackedEntityAttributes();
        }

    }

    private interface TrackedEntityInstanceService {
        @GET("28/trackedEntityInstances")
        Call<TrackedEntityObject> trackEntityInstances(@Query("ou") String orgUnits,
                                                       @Query("ouMode") String ouMode,
                                                       @Query("program") String programId,
                                                       @Query("totalPages") boolean showPager,
                                                       @Query("programEnrollmentStartDate") String enrollmentStartDate,
                                                       @Query("programIncidentStartDate") String incientStartDate,
                                                       @Query("filter") List<String> filter,
                                                       @Query("fields") String fields);
    }
}
