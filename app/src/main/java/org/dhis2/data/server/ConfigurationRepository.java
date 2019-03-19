package org.dhis2.data.server;

import org.hisp.dhis.android.core.configuration.Configuration;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import okhttp3.HttpUrl;

public interface ConfigurationRepository {

    @NonNull
    Observable<Configuration> configure(@NonNull HttpUrl baseUrl);

    @NonNull
    Observable<Configuration> get();

    @NonNull
    Observable<Integer> remove();
}
