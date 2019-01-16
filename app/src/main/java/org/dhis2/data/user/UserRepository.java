package org.dhis2.data.user;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.user.UserCredentialsModel;
import org.hisp.dhis.android.core.user.UserModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface UserRepository {

    @NonNull
    Flowable<UserCredentialsModel> credentials();

    @NonNull
    Flowable<UserModel> me();

    @NonNull
    Observable<List<OrganisationUnitModel>> myOrgUnits();
}
