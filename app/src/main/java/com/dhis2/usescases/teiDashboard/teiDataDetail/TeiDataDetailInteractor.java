package com.dhis2.usescases.teiDashboard.teiDataDetail;

import android.util.Log;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.DashboardRepository;
import com.dhis2.usescases.teiDashboard.TeiDashboardInteractor;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by frodriguez on 12/13/2017.
 */

public class TeiDataDetailInteractor  implements TeiDataDetailContracts.Interactor {

    private TeiDataDetailContracts.View view;

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private D2 d2;
    private TrackedEntityInstance trackedEntityInstance;

    private String programUid;

    public TeiDataDetailInteractor(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(TeiDataDetailContracts.View view, String uid, String programUid) {
        this.view = view;
        this.programUid = programUid;
        getTrackedEntityInstance(uid);
    }

    @Override
    public void getTrackedEntityInstance(String teiUid) {
        d2.retrofit().create(TrackedEntityInstanceService.class).trackEntityInstances(teiUid, programUid, "*")
                .enqueue(new Callback<TrackedEntityInstance>() {
                    @Override
                    public void onResponse(Call<TrackedEntityInstance> call, Response<TrackedEntityInstance> response) {
                        trackedEntityInstance = response.body();
                        getProgramData(programUid);
                    }

                    @Override
                    public void onFailure(Call<TrackedEntityInstance> call, Throwable t) {

                    }
                });

    }

    @Override
    public void getProgramData(String programId) {
        Enrollment selectedEnrollment = null;
        for (Enrollment enrollment : trackedEntityInstance.enrollments())
            if (enrollment.program().equals(programId))
                selectedEnrollment = enrollment;

        if (selectedEnrollment != null && programId != null)
            Observable.zip(dashboardRepository.getProgramData(programId),
                    dashboardRepository.getProgramStages(programId),
                    metadataRepository.getProgramTrackedEntityAttributes(programId),
                    metadataRepository.getOrganisatuibUnit(selectedEnrollment.organisationUnit()),
                    metadataRepository.getProgramModelFromEnrollmentList(trackedEntityInstance.enrollments()),
                    metadataRepository.getRelationshipType(programId),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> view.setData(trackedEntityInstance, data),
                            throwable -> Log.d("ERROR", throwable.getMessage()));
        else if (programId != null)
            Observable.zip(dashboardRepository.getProgramData(programId),
                dashboardRepository.getProgramStages(programId),
                metadataRepository.getProgramTrackedEntityAttributes(programId),
                metadataRepository.getProgramModelFromEnrollmentList(trackedEntityInstance.enrollments()),
                metadataRepository.getRelationshipType(programId),
                DashboardProgramModel::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setData(trackedEntityInstance, data),
                        throwable -> Log.d("ERROR", throwable.getMessage()));

    }

    public interface TrackedEntityInstanceService {
        @GET("28/trackedEntityInstances/{uid}")
        Call<TrackedEntityInstance> trackEntityInstances(@Path("uid") String teiUid,
                                                         @Query("program") String programUid,
                                                         @Query("fields") String fields);
    }
}
