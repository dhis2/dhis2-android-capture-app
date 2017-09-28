package com.dhis2.usescases.login;

import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginPresenter implements LoginContracts.Presenter {

    private LoginContracts.View view;
    private LoginContracts.Interactor interactor;

    @NonNull
    private final CompositeDisposable disposable;

    public ObservableField<Boolean> isServerUrlSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserNameSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserPassSet = new ObservableField<>(false);

    @Inject
    LoginPresenter(LoginContracts.View view) {
        this.view = view;
        this.interactor = new LoginInteractor(view);
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isServerUrlSet.set(!view.getBinding().serverUrl.getEditText().getText().toString().isEmpty());
        isUserNameSet.set(!view.getBinding().userName.getEditText().getText().toString().isEmpty());
        isUserPassSet.set(!view.getBinding().userPass.getEditText().getText().toString().isEmpty());
    }

    @UiThread
    @Override
    public void validateCredentials() {
        HttpUrl baseUrl = HttpUrl.parse(canonizeUrl(view.getBinding().serverUrl.getEditText().toString()));
        if (baseUrl == null) {
            return;
        }

        disposable.add(configurationRepository.configure(baseUrl)
                .map((config) -> componentsHandler.createServerComponent(config).userManager())
                .switchMap((userManager) -> userManager.logIn(userName.get(), userPass.get()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(LoginPresenter.this::handleResponse, LoginPresenter.this::handleError));
    }

    private String canonizeUrl(@NonNull String serverUrl) {
        return serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
    }
}