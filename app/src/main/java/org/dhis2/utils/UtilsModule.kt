package org.dhis2.utils

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

@Module
@Singleton
class UtilsModule {

    @Provides
    @Singleton
    internal fun currentDateProvider(): CurrentDateProvider {
        return CurrentDateProviderImpl()
    }

    @Provides
    @Singleton
    internal fun rulesUtilsProvider(codeGenerator: CodeGenerator): RulesUtilsProvider {
        return RulesUtilsProviderImpl(codeGenerator)
    }
}
