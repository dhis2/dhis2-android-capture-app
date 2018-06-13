package com.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.os.Handler;
import android.view.View;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.server.ConfigurationRepository;
import com.dhis2.usescases.main.MainActivity;
import com.dhis2.usescases.qrScanner.QRActivity;
import com.dhis2.utils.Constants;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

public class LoginPresenter implements LoginContracts.Presenter {

    private final ConfigurationRepository configurationRepository;
    private final MetadataRepository metadataRepository;
    private LoginContracts.View view;
    private LoginContracts.Interactor interactor;

    public ObservableField<Boolean> isServerUrlSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserNameSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserPassSet = new ObservableField<>(false);

    LoginPresenter(ConfigurationRepository configurationRepository, MetadataRepository metadataRepository) {
        this.configurationRepository = configurationRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(LoginContracts.View view) {
        this.view = view;
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(view.getContext()));
        this.interactor = new LoginInteractor(view, configurationRepository, metadataRepository, firebaseJobDispatcher);
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
    public void syncNext(LoginActivity.SyncState syncState, SyncResult syncResult) {
        if (syncResult.isSuccess() || syncState != LoginActivity.SyncState.METADATA)
            switch (syncState) {
                case METADATA:
                    interactor.syncEvents();
                    break;
                case EVENTS:
                    interactor.syncReservedValues();
                    interactor.syncTrackedEntities();
                    break;
                case TEI:
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                    break;
            }
        else {
            view.displayMessage("Something went wrong during syncronisation");
            new Handler().postDelayed(() -> interactor.logOut(), 1500);
        }
    }
}