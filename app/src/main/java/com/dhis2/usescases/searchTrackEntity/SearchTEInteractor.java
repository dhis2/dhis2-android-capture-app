package com.dhis2.usescases.searchTrackEntity;

import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.programDetail.TrackedEntityObject;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.HashMap;
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
import retrofit2.http.QueryMap;
import timber.log.Timber;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class SearchTEInteractor implements SearchTEContractsModule.Interactor {
    private final String ouMode = "DESCENDANTS";

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;
    private SearchTEContractsModule.View view;
    private List<OptionModel> optionList;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private HashMap<String, String> filterQuery = new HashMap<>();
    private D2 d2;

    private String selectedProgramId;
    private List<OrganisationUnitModel> orgList;

    @Inject
    public SearchTEInteractor(D2 d2, SearchRepository searchRepository, UserRepository userRepository) {
        this.searchRepository = searchRepository;
        this.userRepository = userRepository;
        this.d2 = d2;
    }

    @Override
    public void init(SearchTEContractsModule.View view) {
        this.view = view;
        optionList = new ArrayList<>();
        getTrackedEntityAttributes();
        filterQuery = new HashMap<>();
    }

    @Override
    public void getTrackedEntityAttributes() {
        compositeDisposable.add(searchRepository.programAttributes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setForm(data),
                        Timber::d)
        );

        compositeDisposable.add(userRepository.myOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgsUnits -> {
                            orgList = new ArrayList<>();
                            orgList.addAll(orgsUnits);
                        },
                        Timber::d)
        );

    }

    @Override
    public void getProgramTrackedEntityAttributes() {

    }

    @Override
    public Observable<List<OptionModel>> getOptions(String optionSetId) {

        return searchRepository.optionSet(optionSetId);
    }

    //Gets all trackedEntityInstances applaying queries (Should be changed when sdk is ready
    @Override
    public void filterTrackEntities(String filter) {

        filterQuery.put("filter", filter);

        String orgQuey = "";
        for (int i = 0; i < orgList.size(); i++) {
            orgQuey = orgQuey.concat(orgList.get(i).uid());
            if (i < orgList.size() - 1)
                orgQuey = orgQuey.concat(",");
        }

        d2.retrofit().create(TrackedEntityInstanceService.class).trackEntityInstances(orgQuey, ouMode, selectedProgramId, true, filterQuery).enqueue(new Callback<TrackedEntityObject>() {
            @Override
            public void onResponse(Call<TrackedEntityObject> call, Response<TrackedEntityObject> response) {
                view.swapData(response.body());
            }

            @Override
            public void onFailure(Call<TrackedEntityObject> call, Throwable t) {

            }
        });

    }

    private interface TrackedEntityInstanceService {
        @GET("28/trackedEntityInstances")
        Call<TrackedEntityObject> trackEntityInstances(@Query("ou") String orgUnits,
                                                       @Query("ouMode") String ouMode,
                                                       @Query("program") String programId,
                                                       @Query("totalPages") boolean showPager,
                                                       @QueryMap HashMap<String, String> filterQuery);
    }
}
