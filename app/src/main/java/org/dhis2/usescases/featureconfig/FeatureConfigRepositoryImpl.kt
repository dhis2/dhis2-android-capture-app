package org.dhis2.usescases.featureconfig

class FeatureConfigRepositoryImpl() : FeatureConfigRepository {

    private var _features: LinkedHashMap<String, Boolean> = linkedMapOf(
        "Feature 1" to false,
        "Feature 2" to false
    )
    override val featuresList: List<Feature>
        get() = _features.map { Feature(it.key, it.value) }

    override fun updateItem(feature: Feature) {
        val updatedFeature = feature.copy(enable = !feature.enable)
        _features[updatedFeature.name] = updatedFeature.enable
    }
}

//TODO store updates in preferences and combine list with cache and persistence