package org.dhis2.data.server;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;

import io.reactivex.Observable;
import retrofit2.Response;

public interface UserManager {

    @NonNull
    Observable<User> logIn(@NonNull String username, @NonNull String password);

    @NonNull
    Observable<Boolean> isUserLoggedIn();

    D2 getD2();
}
