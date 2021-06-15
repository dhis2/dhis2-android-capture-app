package org.dhis2.usescases.featureconfig

import org.dhis2.usescases.featureconfig.Feature.FEATURE_1
import org.dhis2.usescases.featureconfig.Feature.FEATURE_2

class FeatureConfigRepositoryImpl() : FeatureConfigRepository {

    private var _features: LinkedHashMap<Feature, Boolean> = linkedMapOf(
        FEATURE_1 to false,
        FEATURE_2 to false
    )
    override val featuresList: List<FeatureState>
        get() = _features.map { FeatureState(it.key, it.value) }

    override fun updateItem(featureState: FeatureState) {
        val updatedFeature = featureState.copy(enable = !featureState.enable)
        _features[updatedFeature.feature] = updatedFeature.enable
    }
}

//TODO store updates in preferences and combine list with cache and persistence