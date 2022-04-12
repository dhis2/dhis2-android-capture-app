package org.dhis2.data.server;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode;
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import kotlin.Pair;

public interface UserManager {

    @NonNull
    Observable<User> logIn(@NonNull String username, @NonNull String password, @NonNull String serverUrl);

    @NonNull
    Observable<IntentWithRequestCode> logIn(@NonNull OpenIDConnectConfig config);

    @NonNull
    Observable<User> handleAuthData(@NonNull String serverUrl, @Nullable Intent data, int requestCode);

    @NonNull
    Observable<Boolean> isUserLoggedIn();

    @NonNull
    Single<String> userInitials();

    @NonNull
    Single<String> userFullName();

    @NonNull
    Single<String> userName();

    D2 getD2();

    @NonNull
    Single<Pair<String, Integer>> getTheme();

    Completable logout();

    boolean allowScreenShare();
}
