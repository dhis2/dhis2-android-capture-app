package org.dhis2.commons.featureconfig.data

import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.featureconfig.model.FeatureState

interface FeatureConfigRepository {

    val featuresList: List<FeatureState>
    fun updateItem(featureState: FeatureState)
    fun isFeatureEnable(feature: Feature): Boolean
}
