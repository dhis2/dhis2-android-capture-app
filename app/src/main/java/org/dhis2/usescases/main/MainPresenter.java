package org.dhis2.usescases.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.Gravity;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.user.UserRepository;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.UserModel;

import androidx.work.WorkManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

final class MainPresenter implements MainContracts.Presenter {

    private final UserRepository userRepository;
    private final MetadataRepository metadataRepository;
    private MainContracts.View view;
    private CompositeDisposable compositeDisposable;


    private final D2 d2;

    MainPresenter(@NonNull D2 d2, UserRepository userRepository, MetadataRepository metadataRepository) {
        this.d2 = d2;
        this.userRepository = userRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(MainContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();

        ConnectableFlowable<UserModel> userObservable = userRepository.me()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();


        compositeDisposable.add(userObservable
                .map(this::username)
                .subscribe(
                        view.renderUsername(),
                        Timber::e));

        compositeDisposable.add(
                metadataRepository.getDefaultCategoryOptionId()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                id -> {
                                    SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                                            Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                                    prefs.edit().putString(Constants.DEFAULT_CAT_COMBO, id).apply();
                                },
                                Timber::e
                        )
        );

        compositeDisposable.addAll(userObservable.connect());
    }

    @Override
    public void logOut() {
        try {
            WorkManager.getInstance().cancelAllWork();
            d2.logout().call();
            view.startActivity(LoginActivity.class, null, true, true, null);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void blockSession(String pin) {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("SessionLocked", true).apply();
        if (pin != null) {
            prefs.edit().putString("pin", pin).apply();
        }
        WorkManager.getInstance().cancelAllWork();
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
    public void getErrors() {
        compositeDisposable.add(
                metadataRepository.getSyncErrors()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.showSyncErrors(data),
                                Timber::e
                        )
        );
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