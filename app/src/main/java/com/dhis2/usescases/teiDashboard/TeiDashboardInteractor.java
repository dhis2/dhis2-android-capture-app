package com.dhis2.usescases.teiDashboard;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardInteractor implements TeiDashboardContracts.Interactor {

    private final DashboardRepository dashboardRepository;
    private D2 d2;
    private CompositeDisposable disposable;
    private TeiDashboardContracts.View view;

    @Inject
    public TeiDashboardInteractor(D2 d2, DashboardRepository dashboardRepository) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        disposable = new CompositeDisposable();
    }

    @Override
    public void init(TeiDashboardContracts.View view, String teiUid) {
        this.view = view;
        getTrackedEntityInstance(teiUid);
    }

    @Override
    public void getTrackedEntityInstance(String teiUid) {
        d2.retrofit().create(TrackedEntityInstanceService.class).trackEntityInstances(teiUid, "*")
                .enqueue(new Callback<TrackedEntityInstance>() {
                    @Override
                    public void onResponse(Call<TrackedEntityInstance> call, Response<TrackedEntityInstance> response) {
                        view.setData(response.body());
                    }

                    @Override
                    public void onFailure(Call<TrackedEntityInstance> call, Throwable t) {

                    }
                });

    }


    private interface TrackedEntityInstanceService {
        @GET("28/trackedEntityInstances/{uid}")
        Call<TrackedEntityInstance> trackEntityInstances(@Path("uid") String teiUid,
                                                         @Query("fields") String fields);
    }

}
