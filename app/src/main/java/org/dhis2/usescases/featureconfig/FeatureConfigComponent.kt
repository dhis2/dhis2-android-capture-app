package org.dhis2.usescases.featureconfig

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.prefs.PreferenceModule

@PerActivity
@Subcomponent(
    modules = [
        FeatureConfigModule::class,
        PreferenceModule::class
    ]
)
interface FeatureConfigComponent {
    fun inject()
}