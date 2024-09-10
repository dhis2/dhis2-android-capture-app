package org.dhis2.commons.featureconfig.data

import com.google.gson.Gson
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.featureconfig.model.FeatureOptions
import org.dhis2.commons.featureconfig.model.FeatureState
import org.hisp.dhis.android.core.D2
import javax.inject.Inject

class FeatureConfigRepositoryImpl @Inject constructor(
    val d2: D2,
) : FeatureConfigRepository {

    val localDataStore = d2.dataStoreModule().localDataStore()

    override val featuresList: List<FeatureState>
        get() = Feature.entries.map {
            val options = featureOptions(it)
            FeatureState(
                it,
                isFeatureEnable(it),
                when (options) {
                    is FeatureOptions.ResponsiveHome -> options.totalItems != null
                    null -> true
                },
                featureOptions(it),
            )
        }

    override fun updateItem(featureState: FeatureState) {
        localDataStore.value(
            featureState.feature.name,
        ).blockingSet(
            featureState.enable.toString(),
        )
        localDataStore.value(
            "${featureState.feature.name}_extras",
        ).blockingSet(
            Gson().toJson(featureState.extras),
        )
    }

    override fun isFeatureEnable(feature: Feature): Boolean {
        return if (localDataStore.value(feature.name).blockingExists()) {
            localDataStore.value(feature.name).blockingGet()?.value()?.toBooleanStrictOrNull()
                ?: false
        } else {
            d2.settingModule().generalSetting()
                .hasExperimentalFeature(feature.name).blockingGet()
        }
    }

    private fun featureOptions(feature: Feature): FeatureOptions? {
        return when (feature) {
            Feature.AUTO_LOGOUT -> null
            Feature.RESPONSIVE_HOME -> FeatureOptions.ResponsiveHome(totalItems = getResponsiveHomeTotalItems())
        }
    }

    private fun getResponsiveHomeTotalItems(): Int? {
        val value = localDataStore
            .value(key = "${Feature.RESPONSIVE_HOME.name}_extras").blockingGet()?.value()
        return value?.let { Gson().fromJson(value, FeatureOptions.ResponsiveHome::class.java).totalItems }
    }
}
