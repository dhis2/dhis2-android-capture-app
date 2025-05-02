package org.dhis2.common.featureConfig

import org.dhis2.commons.featureconfig.data.FeatureConfigRepositoryImpl
import org.dhis2.commons.featureconfig.model.Feature

class FeatureConfigRobot (private val featureConfigRepository: FeatureConfigRepositoryImpl) {

    fun enableFeature(feature: Feature) {
        featureConfigRepository.updateItem(featureConfigRepository.featuresList.first { it.feature == feature }.copy(enable = true))
    }

    fun disableFeature(feature: Feature) {
        featureConfigRepository.updateItem(featureConfigRepository.featuresList.first { it.feature == feature }.copy(enable = false))
    }

}