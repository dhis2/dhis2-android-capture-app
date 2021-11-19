package org.dhis2.commons.featureconfig.di

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.featureconfig.ui.FeatureConfigView

@PerActivity
@Subcomponent(modules = [FeatureConfigActivityModule::class])
interface FeatureConfigActivityComponent {
    fun inject(featureConfigView: FeatureConfigView)
}
