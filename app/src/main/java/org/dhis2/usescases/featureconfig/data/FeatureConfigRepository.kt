package org.dhis2.usescases.featureconfig.data

import org.dhis2.usescases.featureconfig.model.FeatureState

interface FeatureConfigRepository {

    val featuresList: List<FeatureState>
    fun updateItem(featureState: FeatureState)
}
