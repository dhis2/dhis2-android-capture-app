package org.dhis2.usescases.about;

import androidx.annotation.NonNull;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.user.UserRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */

public class AboutPresenterImpl implements AboutContracts.AboutPresenter {
    private final MetadataRepository metadata;
    private final UserRepository userRepository;
    private CompositeDisposable compositeDisposable;

    AboutPresenterImpl(@NonNull MetadataRepository metadataRepository, @NonNull UserRepository userRepository) {
        this.metadata = metadataRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void init(AboutContracts.AboutView view) {

        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(userRepository.credentials()
                .cacheWithInitialCapacity(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::renderUserCredentials,
                        Timber::e
                ));

        compositeDisposable.add(metadata.getServerUrl()
                .cacheWithInitialCapacity(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
