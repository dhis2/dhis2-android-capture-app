package org.dhis2.commons.featureconfig.model

data class FeatureState(
    val feature: Feature,
    val enable: Boolean = false,
    val canBeEnabled: Boolean = true,
    val extras: FeatureOptions? = null,
)

sealed class FeatureOptions {
    data class ResponsiveHome(val totalItems: Int?) : FeatureOptions()
}
