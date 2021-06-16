package org.dhis2.usescases.featureconfig

import org.dhis2.data.prefs.PreferenceProvider
import javax.inject.Inject

class FeatureConfigRepositoryImpl @Inject constructor(val preferences: PreferenceProvider) :
    FeatureConfigRepository {

    override val featuresList: List<FeatureState>
        get() = Feature.values().map {
            FeatureState(it, preferences.getBoolean(it.name, false))
        }

    override fun updateItem(featureState: FeatureState) {
        preferences.setValue(featureState.feature.name, !featureState.enable)
    }
}
