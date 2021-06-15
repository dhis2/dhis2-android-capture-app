package org.dhis2.usescases.featureconfig

interface FeatureConfigRepository {

    val featuresList: List<FeatureState>
    fun updateItem(featureState: FeatureState)
}