package com.data.server;

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
        return Observable.defer(new Callable<ObservableSource<? extends ConfigurationModel>>() {
            @Override
            public ObservableSource<? extends ConfigurationModel> call() throws Exception {
                return Observable.fromCallable(
                        new Callable<ConfigurationModel>() {
                            @Override
                            public ConfigurationModel call() throws Exception {
                                return configurationManager.configure(baseUrl);
                            }
                        });
            }
        });


    }

    @NonNull
    @Override
    public Observable<ConfigurationModel> get() {
        return Observable.defer(new Callable<ObservableSource<? extends ConfigurationModel>>() {
            @Override
            public ObservableSource<? extends ConfigurationModel> call() throws Exception {
                ConfigurationModel configuration = configurationManager.get();
                if (configuration != null) {
                    return Observable.just(configuration);
                }

                return Observable.empty();
            }
        });
    }

    @NonNull
    @Override
    public Observable<Integer> remove() {
        return Observable.defer(() -> Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return configurationManager.remove();
            }
        }));
    }
}
