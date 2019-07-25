package org.dhis2.usescases.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

final class MainPresenter implements MainContracts.Presenter {

    private final MetadataRepository metadataRepository;
    private MainContracts.View view;
    private CompositeDisposable compositeDisposable;


    private final D2 d2;
    private SharePreferencesProvider provider;

    MainPresenter(SharePreferencesProvider provider, @NonNull D2 d2, MetadataRepository metadataRepository) {
        this.d2 = d2;
        this.provider = provider;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(MainContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();
        view.setPreferences(provider);
        compositeDisposable.add(Observable.defer(() -> Observable.just(d2.userModule().user.get()))
                .map(this::username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.renderUsername(),
                        Timber::e
                )
        );


        compositeDisposable.add(
                metadataRepository.getDefaultCategoryOptionId()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                id -> {
                                    provider.sharedPreferences().putString(Constants.DEFAULT_CAT_COMBO, id);
                                },
                                Timber::e
                        )
        );


        compositeDisposable.add(
                metadataRepository.getDefaultCategoryOptionComboId()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                id -> {
                                    provider.sharedPreferences().putString(Constants.PREF_DEFAULT_CAT_OPTION_COMBO, id);
                                },
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );
    }

    @Override
    public void logOut() {
        compositeDisposable.add(d2.userModule().logOut()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    WorkManager.getInstance().cancelAllWork();
                    view.startActivity(LoginActivity.class, null, true, true, null);
                }, Timber::e)
        );
    }

    @Override
    public void blockSession(String pin) {
        provider.sharedPreferences().putBoolean("SessionLocked", true);
        if (pin != null) {
            provider.sharedPreferences().putString("pin", pin);
        }
        WorkManager.getInstance().cancelAllWork();
//        view.startActivity(LoginActivity.class, null, true, true, null);
        view.back();
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
        view.showSyncErrors(metadataRepository.getSyncErrors());
    }

    @Override
    public void onDetach() {
        compositeDisposable.clear();
    }

    @Override
    public void onMenuClick() {
        view.openDrawer(Gravity.START);
    }

    //    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    private String username(@NonNull User user) {
        return String.format("%s %s",
                isEmpty(user.firstName()) ? "" : user.firstName(),
                isEmpty(user.surname()) ? "" : user.surname());
    }

}