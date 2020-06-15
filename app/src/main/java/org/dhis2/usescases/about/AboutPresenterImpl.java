package org.dhis2.usescases.about;

import androidx.annotation.NonNull;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.user.UserRepository;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */

public class AboutPresenterImpl implements AboutContracts.AboutPresenter {
    private final D2 d2;
    private final UserRepository userRepository;
    public CompositeDisposable compositeDisposable;
    private SchedulerProvider provider;

    AboutPresenterImpl(@NonNull D2 d2,
                       SchedulerProvider provider,
                       @NonNull UserRepository userRepository) {
        this.d2 = d2;
        this.provider = provider;
        this.userRepository = userRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(AboutContracts.AboutView view) {

        compositeDisposable.add(userRepository.credentials()
                .cacheWithInitialCapacity(1)
                .subscribeOn(provider.io())
                .observeOn(provider.ui())
                .subscribe(
                        view::renderUserCredentials,
                        Timber::e
                ));

        compositeDisposable.add(
                d2.systemInfoModule().systemInfo().get().toObservable().map(SystemInfo::contextPath)
                .cacheWithInitialCapacity(1)
                .subscribeOn(provider.io())
                .observeOn(provider.ui())
                .subscribe(
                        view::renderServerUrl,
                        Timber::e
                ));
    }

    @Override
    public void onPause() {
        compositeDisposable.clear();
    }
}
