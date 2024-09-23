package org.dhis2.commons.featureconfig.data

import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.featureconfig.model.FeatureState
import org.dhis2.commons.prefs.PreferenceProvider
import javax.inject.Inject

class FeatureConfigRepositoryImpl @Inject constructor(
    val preferences: PreferenceProvider,
) : FeatureConfigRepository {

    override val featuresList: List<FeatureState>
        get() = Feature.entries
            .filter { it.name != Feature.COMPOSE_FORMS.name }
            .map { FeatureState(it, isFeatureEnable(it)) }

    override fun updateItem(featureState: FeatureState) {
        preferences.setValue(featureState.feature.name, !featureState.enable)
    }

    override fun isFeatureEnable(feature: Feature): Boolean {
        return when {
            feature.name == Feature.COMPOSE_FORMS.name -> {
                val fromDevelopment = preferences.getBoolean("SET_FROM_TESTING", false)
                if (fromDevelopment) {
                    preferences.getBoolean(feature.name, false)
                } else {
                    true
                }
            }

            preferences.contains(feature.name) -> {
                preferences.getBoolean(feature.name, false)
            }

            else -> false
        }
    }
}
