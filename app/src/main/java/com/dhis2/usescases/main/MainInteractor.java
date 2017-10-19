package com.dhis2.usescases.main;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainInteractor implements MainContracts.Interactor {

    private D2 d2;

    MainInteractor(D2 d2) {
        this.d2 = d2;
    }


    @Override
    public Call<Payload<OrganisationUnit>> getOrgUnits() {
        MyOrganisationUnitService orgUnitService = d2.retrofit().create(MyOrganisationUnitService.class);

        return orgUnitService.getOrganisationUnitChilds("true", "id,level,parent,shortName");
    }


    public interface MyOrganisationUnitService {

        @GET("organisationUnits/ImspTQPwCqd")
        Call<Payload<OrganisationUnit>> getOrganisationUnitChilds(
                @Query("includeDescendants") String includeDescentants,
                @Query("fields") String fields
        );
    }


}