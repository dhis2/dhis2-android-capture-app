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

import javax.inject.Inject;

public class LoginPresenter implements LoginContractsModule.Presenter {

    private LoginContractsModule.View view;
    private LoginContractsModule.Interactor interactor;


    public ObservableField<Boolean> isServerUrlSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserNameSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserPassSet = new ObservableField<>(false);

    @Inject
    LoginPresenter(LoginContractsModule.View view, ConfigurationRepository configurationRepository) {
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
        interactor.validateCredentials(view.getBinding().serverUrl.getEditText().getText().toString(),
                view.getBinding().userName.getEditText().getText().toString(),
                view.getBinding().userPass.getEditText().getText().toString());
    }

    @Override
    public void onQRClick(View v) {
        Intent intent = new Intent(view.getContext(), QRActivity.class);
        view.getAbstractActivity().startActivityForResult(intent, Constants.RQ_QR_SCANNER);
    }

    public void unlockSession(String pin) {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        if (prefs.getString("pin", "").equals(pin)) {
            prefs.edit().putBoolean("SessionLocked", false).apply();
            view.startActivity(MainActivity.class, null, true, true, null);
        }
    }
}