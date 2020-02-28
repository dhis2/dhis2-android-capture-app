package org.dhis2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

import org.apache.commons.jexl2.JexlEngine;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.CodeGeneratorImpl;
import org.dhis2.utils.ExpressionEvaluatorImpl;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 10/10/2017.
 */
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
    JexlEngine jexlEngine() {
        return new JexlEngine();
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

    @Provides
    @Singleton
    WorkManager workManager(){
        return WorkManager.getInstance(application.getApplicationContext());
    }

    @Provides
    @Singleton
    FilterManager filterManager() {
        return FilterManager.getInstance();
    }


}
