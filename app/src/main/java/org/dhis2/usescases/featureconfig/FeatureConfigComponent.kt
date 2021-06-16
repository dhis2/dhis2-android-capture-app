package org.dhis2.usescases.featureconfig

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

@PerActivity
@Subcomponent(
    modules = [
        FeatureConfigModule::class
    ]
)
interface FeatureConfigComponent {
    fun inject(view: FeatureConfigView)
}