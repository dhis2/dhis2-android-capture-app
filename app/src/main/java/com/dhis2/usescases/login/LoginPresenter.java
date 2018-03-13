package com.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.view.View;

import com.dhis2.data.server.ConfigurationRepository;
import com.dhis2.usescases.main.MainActivity;
import com.dhis2.usescases.qrScanner.QRActivity;
import com.dhis2.utils.Constants;

import org.hisp.dhis.android.core.D2;

public class LoginPresenter implements LoginContracts.Presenter {

    private final ConfigurationRepository configurationRepository;
    private LoginContracts.View view;
    private LoginContracts.Interactor interactor;

    public ObservableField<Boolean> isServerUrlSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserNameSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserPassSet = new ObservableField<>(false);

    LoginPresenter(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public void init(LoginContracts.View view) {
        this.view = view;
        this.interactor = new LoginInteractor(view, configurationRepository);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isServerUrlSet.set(!view.getBinding().serverUrl.getEditText().getText().toString().isEmpty());
        isUserNameSet.set(!view.getBinding().userName.getEditText().getText().toString().isEmpty());
        isUserPassSet.set(!view.getBinding().userPass.getEditText().getText().toString().isEmpty());
    }

    @Override
    public void onButtonClick() {
        view.hideKeyboard();
        interactor.validateCredentials(view.getBinding().serverUrl.getEditText().getText().toString(),
                view.getBinding().userName.getEditText().getText().toString(),
                view.getBinding().userPass.getEditText().getText().toString());
    }

    @Override
    public void onQRClick(View v) {
        Intent intent = new Intent(view.getContext(), QRActivity.class);
        view.getAbstractActivity().startActivityForResult(intent, Constants.RQ_QR_SCANNER);
    }

    @Override
    public ObservableField<Boolean> isServerUrlSet() {
        return isServerUrlSet;
    }

    @Override
    public ObservableField<Boolean> isUserNameSet() {
        return isUserNameSet;
    }

    @Override
    public ObservableField<Boolean> isUserPassSet() {
        return isServerUrlSet;
    }

    @Override
    public void unlockSession(String pin) {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        if (prefs.getString("pin", "").equals(pin)) {
            prefs.edit().putBoolean("SessionLocked", false).apply();
            view.startActivity(MainActivity.class, null, true, true, null);
        }
    }

    @Override
    public void onDestroy() {
        interactor.onDestroy();
    }

    @Override
    public void syncNext(LoginActivity.SyncState syncState) {
        switch (syncState) {
            case METADATA:
                interactor.syncEvents();
                break;
            case EVENTS:
                interactor.syncTrackedEntities();
                break;
            case TEI:
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
                break;
        }

    }
}