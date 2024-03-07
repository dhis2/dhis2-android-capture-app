package org.dhis2.commons.featureconfig.data

import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.featureconfig.model.FeatureState
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.ExperimentalFeature
import javax.inject.Inject

class FeatureConfigRepositoryImpl @Inject constructor(
    val preferences: PreferenceProvider,
    val d2: D2,
) : FeatureConfigRepository {

    val SET_FROM_DEVELOPMENT = "SET_FROM_DEVELOPMENT"

    override val featuresList: List<FeatureState>
        get() = Feature.entries.map {
            FeatureState(it, isFeatureEnable(it))
        }

    override fun updateItem(featureState: FeatureState) {
        if (featureState.feature.name == Feature.COMPOSE_FORMS.name) {
            preferences.setValue(SET_FROM_DEVELOPMENT, true)
        }
        preferences.setValue(featureState.feature.name, !featureState.enable)
    }

    override fun isFeatureEnable(feature: Feature): Boolean {
        return when {
            feature.name == Feature.COMPOSE_FORMS.name -> {
                val fromDevelopment = preferences.getBoolean(SET_FROM_DEVELOPMENT, false)
                if (fromDevelopment) {
                    preferences.getBoolean(feature.name, false)
                } else if (d2.settingModule().generalSetting().blockingExists()) {
                    d2.settingModule().generalSetting()
                        .hasExperimentalFeature(ExperimentalFeature.NewFormLayout).blockingGet()
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
