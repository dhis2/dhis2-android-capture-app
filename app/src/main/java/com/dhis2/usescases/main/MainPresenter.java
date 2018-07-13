package com.dhis2.usescases.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.Gravity;

import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.login.LoginActivity;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.UserModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

final class MainPresenter implements MainContracts.Presenter {

    private final UserRepository userRepository;
    private final FirebaseJobDispatcher jobDispatcher;
    private MainContracts.View view;
    private final CompositeDisposable compositeDisposable;


    private final D2 d2;

    MainPresenter(@NonNull D2 d2, UserRepository userRepository, FirebaseJobDispatcher firebaseJobDispatcher) {
        this.d2 = d2;
        this.compositeDisposable = new CompositeDisposable();
        this.userRepository = userRepository;
        this.jobDispatcher = firebaseJobDispatcher;

    }

    @Override
    public void init(MainContracts.View view) {
        this.view = view;

        ConnectableFlowable<UserModel> userObservable = userRepository.me()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();


        compositeDisposable.add(userObservable
                .map(this::username)
                .subscribe(
                        view.renderUsername(),
                        Timber::e));

        compositeDisposable.addAll(userObservable.connect());
    }

    @Override
    public void logOut() {
        try {
            jobDispatcher.cancelAll();
            d2.logout().call();
            view.startActivity(LoginActivity.class, null, true, true, null);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void blockSession(String pin) {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("SessionLocked", true).apply();
        if (pin != null) {
            prefs.edit().putString("pin", pin).apply();
        }
        jobDispatcher.cancelAll();
        view.startActivity(LoginActivity.class, null, true, true, null);
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    @Override
    public void changeFragment(int id) {
        view.changeFragment(id);
    }

    @Override
    public void onDetach() {
        compositeDisposable.clear();
    }

    @Override
    public void onMenuClick() {
        view.openDrawer(Gravity.START);
    }

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    private String username(@NonNull UserModel user) {
        String username = "";
        if (!isEmpty(user.firstName())) {
            username += user.firstName();
        }

        if (!isEmpty(user.surname())) {
            if (!username.isEmpty()) {
                username += " ";
            }

            username += user.surname();
        }

        return username;
    }

}