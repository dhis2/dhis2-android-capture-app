package org.dhis2.commons.featureconfig.di

interface FeatureConfigComponentProvider {

    fun provideFeatureConfigComponent(): FeatureConfigComponent?
}