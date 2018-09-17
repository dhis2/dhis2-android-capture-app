package org.dhis2.data.server;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import okhttp3.HttpUrl;

public class ConfigurationRepositoryImpl implements ConfigurationRepository {
    @NonNull
    private final ConfigurationManager configurationManager;

    public ConfigurationRepositoryImpl(@NonNull ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @NonNull
    @Override
    public Observable<ConfigurationModel> configure(@NonNull HttpUrl baseUrl) {
        return Observable.defer(() -> Observable.fromCallable(
                () -> configurationManager.configure(baseUrl)));
    }

    @NonNull
    @Override
    public Observable<ConfigurationModel> get() {
        return Observable.defer(() -> {
            ConfigurationModel configuration = configurationManager.get();
            if (configuration != null) {
                return Observable.just(configuration);
            }

            return Observable.empty();
        });
    }

    @NonNull
    @Override
    public Observable<Integer> remove() {
        return Observable.defer(() -> Observable.fromCallable(() -> configurationManager.remove()));
    }
}
