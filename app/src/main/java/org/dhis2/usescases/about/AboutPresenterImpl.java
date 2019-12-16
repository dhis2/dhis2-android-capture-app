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
    private final SchedulerProvider schedulerProvider;
    public CompositeDisposable compositeDisposable;

    AboutPresenterImpl(@NonNull D2 d2, @NonNull UserRepository userRepository, SchedulerProvider schedulerProvider) {
        this.d2 = d2;
        this.userRepository = userRepository;
        this.schedulerProvider = schedulerProvider;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(AboutContracts.AboutView view) {

        compositeDisposable.add(userRepository.credentials()
                .cacheWithInitialCapacity(1)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::renderUserCredentials,
                        Timber::e
                ));

        compositeDisposable.add(
                d2.systemInfoModule().systemInfo().get().toObservable().map(SystemInfo::contextPath)
                .cacheWithInitialCapacity(1)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
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
