package org.dhis2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;

import org.dhis2.commons.resources.ResourceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final App application;

    public AppModule(@NonNull App application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context context() {
        return application;
    }

    @Provides
    @Singleton
    HiltWorkerFactory workerFactory() {
        return application.workerFactory;
    }

    @Provides
    @Singleton
    ResourceManager resources() {
        return new ResourceManager(application);
    }
}
