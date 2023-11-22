package org.dhis2.usescases.troubleshooting

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2

@PerFragment
@Subcomponent(modules = [TroubleshootingModule::class])
interface TroubleshootingComponent {
    fun inject(programFragment: TroubleshootingFragment)
}

@Module
class TroubleshootingModule(private val openLanguageSection: Boolean) {

    @Provides
    fun providesViewModelFactory(
        localeSelector: LocaleSelector,
        repository: TroubleshootingRepository,
    ): TroubleshootingViewModelFactory {
        return TroubleshootingViewModelFactory(
            localeSelector,
            repository,
            openLanguageSection,
        )
    }

    @Provides
    fun providesLocaleSelector(context: Context, d2: D2) = LocaleSelector(context, d2)

    @Provides
    fun provideRepository(resourceManager: ResourceManager, d2: D2) =
        TroubleshootingRepository(d2, resourceManager)
}
