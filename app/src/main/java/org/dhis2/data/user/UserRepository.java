package org.dhis2.data.user;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.user.UserCredentialsModel;
import org.hisp.dhis.android.core.user.UserModel;

import io.reactivex.Flowable;

public interface UserRepository {

    @NonNull
    Flowable<UserCredentialsModel> credentials();

    @NonNull
    Flowable<UserModel> me();

}
