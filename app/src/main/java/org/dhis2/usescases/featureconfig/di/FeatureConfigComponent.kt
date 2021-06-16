package org.dhis2.usescases.featureconfig.di

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity
import org.dhis2.usescases.featureconfig.ui.FeatureConfigView

@PerActivity
@Subcomponent(
    modules = [
        FeatureConfigModule::class
    ]
)
interface FeatureConfigComponent {
    fun inject(view: FeatureConfigView)
}
