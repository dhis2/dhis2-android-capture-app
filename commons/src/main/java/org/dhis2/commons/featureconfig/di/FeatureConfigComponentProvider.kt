package org.dhis2.commons.featureconfig.di

interface FeatureConfigComponentProvider {

    fun provideFeatureConfigActivityComponent(): FeatureConfigActivityComponent?
}
