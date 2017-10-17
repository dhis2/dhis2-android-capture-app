package com.dhis2.usescases.login;

import android.databinding.ObservableField;


import com.dhis2.data.server.ConfigurationRepository;

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
        this.interactor = new LoginInteractor(view,configurationRepository);
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

    public void onTestClick(){
        view.getBinding().serverUrl.getEditText().setText("https://play.dhis2.org/demo");
        view.getBinding().userName.getEditText().setText("admin");
        view.getBinding().userPass.getEditText().setText("district");
    }

}