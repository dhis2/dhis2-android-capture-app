package org.dhis2;

import android.content.Context;
import android.support.annotation.NonNull;

import org.dhis2.data.server.ConfigurationRepository;
import org.dhis2.data.server.ConfigurationRepositoryImpl;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.CodeGeneratorImpl;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
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
final class AppModule {

    private final App application;

    AppModule(@NonNull App application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context context() {
        return application;
    }

    @Provides
    @Singleton
    Duktape duktape() {
        return Duktape.create();
    }

    @Provides
    @Singleton
    ConfigurationManager configurationManager(DatabaseAdapter databaseAdapter) {
        return ConfigurationManagerFactory.create(databaseAdapter);
    }

    @Provides
    @Singleton
    ConfigurationRepository configurationRepository(ConfigurationManager configurationManager) {
        return new ConfigurationRepositoryImpl(configurationManager);
    }

    @Provides
    @Singleton
    CodeGenerator codeGenerator() {
        return new CodeGeneratorImpl();
    }

    @Provides
    @Singleton
    RuleExpressionEvaluator ruleExpressionEvaluator(@NonNull Duktape duktape) {
        return new DuktapeEvaluator(duktape);
    }

    @Provides
    @Singleton
    FirebaseJobDispatcher jobDispatcher(GooglePlayDriver playDriver) {
        return new FirebaseJobDispatcher(playDriver);
    }

    @Provides
    @Singleton
    GooglePlayDriver googlePlayDriver() {
        return new GooglePlayDriver(application);
    }

}
