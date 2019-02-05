package org.dhis2.data.server;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.configuration.ConfigurationModel;

import io.reactivex.Observable;
import okhttp3.HttpUrl;

public interface ConfigurationRepository {

    @NonNull
    Observable<ConfigurationModel> configure(@NonNull HttpUrl baseUrl);

    @NonNull
    Observable<ConfigurationModel> get();

    @NonNull
    Observable<Integer> remove();
}
