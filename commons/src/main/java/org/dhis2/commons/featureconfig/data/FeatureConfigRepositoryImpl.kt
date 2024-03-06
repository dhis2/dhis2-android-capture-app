package org.dhis2.commons.featureconfig.data

import javax.inject.Inject
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.featureconfig.model.FeatureState
import org.dhis2.commons.prefs.PreferenceProvider

class FeatureConfigRepositoryImpl @Inject constructor(val preferences: PreferenceProvider) :
    FeatureConfigRepository {

    override val featuresList: List<FeatureState>
        get() = Feature.values().map {
            FeatureState(it, isFeatureEnable(it))
        }

    override fun updateItem(featureState: FeatureState) {
        preferences.setValue(featureState.feature.name, !featureState.enable)
    }

    override fun isFeatureEnable(feature: Feature) = preferences.getBoolean(feature.name, false)
}
