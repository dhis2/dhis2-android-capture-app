package org.dhis2

import android.content.Context

import org.apache.commons.jexl2.JexlEngine
import org.dhis2.data.server.ConfigurationRepository
import org.dhis2.data.server.ConfigurationRepositoryImpl
import org.dhis2.utils.CodeGenerator
import org.dhis2.utils.CodeGeneratorImpl
import org.dhis2.utils.ExpressionEvaluatorImpl
import org.hisp.dhis.android.core.configuration.ConfigurationManager
import org.hisp.dhis.android.core.configuration.ConfigurationManagerFactory
import org.hisp.dhis.android.core.data.database.DatabaseAdapter
import org.hisp.dhis.rules.RuleExpressionEvaluator

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/**
 * QUADRAM. Created by ppajuelo on 10/10/2017.
 */
@Module
class AppModule(private val application: App) {

    @Provides
    @Singleton
    internal fun context(): Context {
        return application
    }

    @Provides
    @Singleton
    internal fun jexlEngine(): JexlEngine {
        return JexlEngine()
    }

    @Provides
    @Singleton
    internal fun configurationManager(databaseAdapter: DatabaseAdapter): ConfigurationManager {
        return ConfigurationManagerFactory.create(databaseAdapter)
    }

    @Provides
    @Singleton
    internal fun configurationRepository(configurationManager: ConfigurationManager): ConfigurationRepository {
        return ConfigurationRepositoryImpl(configurationManager)
    }

    @Provides
    @Singleton
    internal fun codeGenerator(): CodeGenerator {
        return CodeGeneratorImpl()
    }

    @Provides
    @Singleton
    internal fun ruleExpressionEvaluator(jexlEngine: JexlEngine): RuleExpressionEvaluator {
        return ExpressionEvaluatorImpl(jexlEngine)
    }


}
