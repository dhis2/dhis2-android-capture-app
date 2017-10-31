package com.dhis2.usescases.programDetail;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailInteractor implements ProgramDetailContractModule.Interactor {

    private D2 d2;
    private String ouMode = "DESCENDANTS";

    @Inject
    ProgramDetailInteractor(D2 d2) {
        this.d2 = d2;
    }

    @Override
    public void getData() {
        d2.retrofit().create(TrackedEntityInstanceService.class).trackEntityInstances("DiszpKrYNg8", ouMode, "IpHINAT79UW").enqueue(new Callback<TrackedEntityObject>() {
            @Override
            public void onResponse(Call<TrackedEntityObject> call, Response<TrackedEntityObject> response) {

            }

            @Override
            public void onFailure(Call<TrackedEntityObject> call, Throwable t) {

            }
        });

    }

    private interface TrackedEntityInstanceService {
        @GET("28/trackedEntityInstances")
        Call<TrackedEntityObject> trackEntityInstances(@Query("ou") String orgUnits, @Query("ouMode") String ouMode, @Query("program") String programId);
    }
}
