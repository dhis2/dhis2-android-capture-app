package com.dhis2;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.dhis2.data.server.ConfigurationRepository;
import com.dhis2.data.server.ConfigurationRepositoryImpl;
import com.squareup.duktape.Duktape;

import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationManagerFactory;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.android.DuktapeEvaluator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Module
public class AppModule {
    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    Duktape duktape(){
        return Duktape.create();
    }
    @Provides
    @Singleton
    ConfigurationRepository provideConfigurationRepository(ConfigurationManager configurationManager) {
        return new ConfigurationRepositoryImpl(configurationManager);
    }

    @Provides
    @Singleton
    ConfigurationManager configurationManager(DatabaseAdapter databaseAdapter) {
        return ConfigurationManagerFactory.create(databaseAdapter);
    }
    @Provides
    @Singleton
    RuleExpressionEvaluator ruleExpressionEvaluator(@NonNull Duktape duktape) {
        return new DuktapeEvaluator(duktape);
    }

}
