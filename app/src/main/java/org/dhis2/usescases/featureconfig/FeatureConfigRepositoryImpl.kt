package org.dhis2.usescases.featureconfig

import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.usescases.featureconfig.Feature.FEATURE_1
import org.dhis2.usescases.featureconfig.Feature.FEATURE_2
import javax.inject.Inject

class FeatureConfigRepositoryImpl @Inject constructor(val preferences: PreferenceProvider) :
    FeatureConfigRepository {

    private var _features: LinkedHashMap<Feature, Boolean> = linkedMapOf(
        FEATURE_1 to false,
        FEATURE_2 to false
    )

    override val featuresList: List<FeatureState>
        get() = _features.map {
            FeatureState(it.key, preferences.getBoolean(it.key.name, it.value))
        }

    override fun updateItem(featureState: FeatureState) {
        preferences.setValue(featureState.feature.name, !featureState.enable)
    }
}
