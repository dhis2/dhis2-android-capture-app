package com.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.View;

import com.dhis2.App;
import com.dhis2.data.server.ConfigurationRepository;
import com.dhis2.data.server.UserManager;
import com.dhis2.data.service.SyncService;
import com.dhis2.domain.responses.TEIResponse;
import com.dhis2.domain.responses.TrackedEntityInstance;
import com.dhis2.usescases.main.MainActivity;

import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;
import org.hisp.dhis.android.core.enrollment.EnrollmentHandler;
import org.hisp.dhis.android.core.enrollment.EnrollmentStoreImpl;
import org.hisp.dhis.android.core.event.EventHandler;
import org.hisp.dhis.android.core.event.EventStoreImpl;
import org.hisp.dhis.android.core.resource.ResourceHandler;
import org.hisp.dhis.android.core.resource.ResourceStoreImpl;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.systeminfo.SystemInfoCall;
import org.hisp.dhis.android.core.systeminfo.SystemInfoService;
import org.hisp.dhis.android.core.systeminfo.SystemInfoStoreImpl;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueHandler;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueStoreImpl;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueHandler;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueStoreImpl;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceEndPointCall;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceHandler;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceStoreImpl;
import org.hisp.dhis.android.core.user.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import timber.log.Timber;

public class LoginInteractor implements LoginContracts.Interactor {

    private LoginContracts.View view;
    private ConfigurationRepository configurationRepository;
    private UserManager userManager;
    @NonNull
    private final CompositeDisposable disposable;

    LoginInteractor(LoginContracts.View view, ConfigurationRepository configurationRepository) {
        this.view = view;
        this.disposable = new CompositeDisposable();
        this.configurationRepository = configurationRepository;
        init();
    }

    private void init() {
        userManager = null;
        if (((App) view.getContext().getApplicationContext()).getServerComponent() != null)
            userManager = ((App) view.getContext().getApplicationContext()).getServerComponent().userManager();

        if (userManager != null) {
            disposable.add(userManager.isUserLoggedIn()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((isUserLoggedIn) -> {
                        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                                "com.dhis2", Context.MODE_PRIVATE);
                        if (isUserLoggedIn && !prefs.getBoolean("SessionLocked", false)) {
                            view.startActivity(MainActivity.class, null, true, true, null);
                        } else if (prefs.getBoolean("SessionLocked", false)) {
                            view.getBinding().unlock.setVisibility(View.VISIBLE);
                        }

                    }, Timber::e));
        }
    }

    @UiThread
    @Override
    public void validateCredentials(@NonNull String serverUrl,
                                    @NonNull String username, @NonNull String password) {
        HttpUrl baseUrl = HttpUrl.parse(canonizeUrl(serverUrl));
        if (baseUrl == null) {
            return;
        }

        disposable.add(configurationRepository.configure(baseUrl)
                .map((config) -> ((App) view.getContext().getApplicationContext()).createServerComponent(config).userManager())
                .switchMap((userManager) -> {
                    this.userManager = userManager;
                    return userManager.logIn(username, password);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        LoginInteractor.this::handleResponse
                        , LoginInteractor.this::handleError));
    }

    @Override
    public void sync() {


        //TODO: TEI sync not working. Get all TEI uids and call external method before SyncService starts
        userManager.getD2().retrofit().create(TESTTrackedEntityInstanceService.class).trackEntityInstances().enqueue(new Callback<TEIResponse>() {
            @Override
            public void onResponse(Call<TEIResponse> call, Response<TEIResponse> response) {
                new TEIAsync(response.body().getTrackedEntityInstances()).execute();

            }

            @Override
            public void onFailure(Call<TEIResponse> call, Throwable t) {

            }
        });
    }

    private void saveTEI(List<TrackedEntityInstance> trackedEntityInstances) {

        DatabaseAdapter databaseAdapter = userManager.getD2().databaseAdapter();

        TrackedEntityInstanceStoreImpl trackedEntityInstanceStore =
                new TrackedEntityInstanceStoreImpl(databaseAdapter);


        TrackedEntityAttributeValueHandler trackedEntityAttributeValueHandler =
                new TrackedEntityAttributeValueHandler(new TrackedEntityAttributeValueStoreImpl(databaseAdapter));

        TrackedEntityDataValueHandler trackedEntityDataValueHandler =
                new TrackedEntityDataValueHandler(new TrackedEntityDataValueStoreImpl(databaseAdapter));

        EnrollmentHandler enrollmentHandler = new EnrollmentHandler(
                new EnrollmentStoreImpl(databaseAdapter), new EventHandler(
                new EventStoreImpl(databaseAdapter), trackedEntityDataValueHandler));

        TrackedEntityInstanceHandler trackedEntityInstanceHandler =
                new TrackedEntityInstanceHandler(
                        trackedEntityInstanceStore,
                        trackedEntityAttributeValueHandler,
                        enrollmentHandler);

        ResourceHandler resourceHandler = new ResourceHandler(new ResourceStoreImpl(databaseAdapter));
        Transaction transaction = databaseAdapter.beginNewTransaction();
        Response response = null;
        try {

            response = new SystemInfoCall(
                    databaseAdapter,
                    new SystemInfoStoreImpl(databaseAdapter),
                    userManager.getD2().retrofit().create(SystemInfoService.class),
                    new ResourceStoreImpl(databaseAdapter)
            ).call();

            if (!response.isSuccessful()) {
                return;
            }

            SystemInfo systemInfo = (SystemInfo) response.body();
            Date serverDate = systemInfo.serverDate();

            for (int i = 0; i < trackedEntityInstances.size(); i++) {
                    try {
                        response = new TrackedEntityInstanceEndPointCall(
                                userManager.getD2().retrofit().create(TrackedEntityInstanceService.class),
                                databaseAdapter,
                                trackedEntityInstanceHandler,
                                resourceHandler,
                                serverDate,
                                trackedEntityInstances.get(i).getTrackedEntityInstance()
                        ).call();
                    } catch (Exception e) {
                        Log.d("TEI ERROR", trackedEntityInstances.get(i).getTrackedEntityInstance() + " - " + e.getMessage());
                    }
            }
            transaction.setSuccessful();

        } catch (Exception e) {
            Log.d("TEI RESPONSE", "ERROR SERVER RESPONSE");
        } finally {
            String data = response != null ? String.valueOf(response.isSuccessful()) : "false";
            Log.d("TEI RESPONSE", "IS SUCCSESS? = " + data);
            transaction.end();

        }
    }

    @Override
    public void handleResponse(@NonNull Response<User> userResponse) {
        Timber.d("Authentication response url: %s", userResponse.raw().request().url().toString());
        Timber.d("Authentication response code: %s", userResponse.code());
        if (userResponse.isSuccessful()) {
            ((App) view.getContext().getApplicationContext()).createUserComponent();
            sync();
            view.saveUsersData();
            view.handleSync();
//            view.startActivity(MainActivity.class, null, true, true, null);
        } else if (userResponse.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            view.hideProgress();
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            view.hideProgress();
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            view.hideProgress();
            view.renderUnexpectedError();
        } else if (userResponse.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            view.hideProgress();
            view.renderServerError();
        }
    }

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);

        if (throwable instanceof IOException) {
            view.hideProgress();
            view.renderInvalidServerUrlError();
        } else {
            view.hideProgress();
            view.renderUnexpectedError();
        }
    }


    private String canonizeUrl(@NonNull String serverUrl) {
        return serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
    }


    //TODO: Currentl, SDK not providing TEI sync. This call is used for user android in android-current
    public interface TESTTrackedEntityInstanceService {
        @GET("28/trackedEntityInstances?ou=ImspTQPwCqd&ouMode=ACCESSIBLE&totalPages=true&paging=false&fields=trackedEntityInstance")
        Call<TEIResponse> trackEntityInstances();
    }

    class TEIAsync extends AsyncTask<String, String, Void> {

        private List<TrackedEntityInstance> list;

        public TEIAsync(List<TrackedEntityInstance> trackedEntityInstances) {
            this.list = trackedEntityInstances;
        }

        @Override
        protected Void doInBackground(String... strings) {

            saveTEI(list);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncService.class));
//            view.startActivity(MainActivity.class, null, true, true, null);
        }
    }

}