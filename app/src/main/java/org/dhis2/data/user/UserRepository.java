package org.dhis2.data.user;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.user.User;
import org.hisp.dhis.android.core.user.UserCredentialsModel;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface UserRepository {

    @NonNull
    Flowable<UserCredentialsModel> credentials();

    @NonNull
    Flowable<User> me();

    @NonNull
    Observable<List<OrganisationUnitModel>> myOrgUnits();
}
