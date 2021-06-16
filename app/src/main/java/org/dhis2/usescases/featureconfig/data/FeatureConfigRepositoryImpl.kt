package org.dhis2.usescases.featureconfig.data

import javax.inject.Inject
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.usescases.featureconfig.model.Feature
import org.dhis2.usescases.featureconfig.model.FeatureState

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
