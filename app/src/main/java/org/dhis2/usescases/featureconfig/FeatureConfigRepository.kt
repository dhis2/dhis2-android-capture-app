package org.dhis2.usescases.featureconfig

interface FeatureConfigRepository {

    val featuresList: List<Feature>
    fun updateItem(feature: Feature)
}