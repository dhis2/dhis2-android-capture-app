package org.dhis2;

import android.content.Context;
import androidx.annotation.NonNull;

import org.apache.commons.jexl2.JexlEngine;
import org.dhis2.data.server.ConfigurationRepository;
import org.dhis2.data.server.ConfigurationRepositoryImpl;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.CodeGeneratorImpl;
import org.dhis2.utils.ExpressionEvaluatorImpl;
import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationManagerFactory;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 10/10/2017.
 */
@Module
public final class AppModule {

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
    JexlEngine jexlEngine() {
        return new JexlEngine();
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
    RuleExpressionEvaluator ruleExpressionEvaluator(@NonNull JexlEngine jexlEngine) {
        return new ExpressionEvaluatorImpl(jexlEngine);
    }


}
