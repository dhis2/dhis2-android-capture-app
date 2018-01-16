package com.dhis2.usescases.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.Gravity;

import com.dhis2.data.service.SyncService;
import com.dhis2.usescases.login.LoginActivity;

import org.hisp.dhis.android.core.D2;

import javax.inject.Inject;

import timber.log.Timber;

final class MainPresenter implements MainContracts.Presenter {

    private MainContracts.View view;
    @Inject
    MainContracts.Interactor interactor;

    private final D2 d2;

    MainPresenter(@NonNull D2 d2,
                  MainContracts.Interactor interactor) {
        this.d2 = d2;
        this.interactor = interactor;
    }

    @Override
    public void init(MainContracts.View view) {
        this.view = view;
//        sync();
        interactor.init(view);
    }

    public void sync() {
        view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncService.class));
    }


    @Override
    public void logOut() {
        try {
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
        view.startActivity(LoginActivity.class, null, true, true, null);
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    @Override
    public void onDetach() {
        interactor.onDettach();
    }

    @Override
    public void onMenuClick() {
        view.openDrawer(Gravity.START);
    }


}