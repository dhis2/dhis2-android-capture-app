package com.dhis2.usescases.main;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.dhis2.data.service.SyncService;
import com.dhis2.data.user.UserRepository;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.UserModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

final class MainPresenter implements MainContracts.Presenter {

    private MainContracts.View view;
    private final UserRepository userRepository;
    private final CompositeDisposable compositeDisposable;
    private final D2 d2;

    MainPresenter(@NonNull D2 d2,
                  @NonNull UserRepository userRepository) {
        this.d2 = d2;
        this.userRepository = userRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(MainContracts.View view) {
        this.view = view;
        sync();
        ConnectableFlowable<UserModel> userObservable = userRepository.me()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();

        compositeDisposable.add(userObservable
                .map(this::username)
                .subscribe(
                        view.renderUsername(),
                        Timber::e));

        compositeDisposable.add(userObservable.connect());


    }

    public void sync() {
        view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncService.class));
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

    @Override
    public void logOut() {
        try {
            d2.logOut().call();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onDetach() {
        compositeDisposable.clear();
    }
}