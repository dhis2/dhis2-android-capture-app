package org.dhis2.form.ui.provider

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.LegendValue
import org.hisp.dhis.android.core.D2

class LegendValueProviderImpl(
    val d2: D2,
    val resourceManager: ResourceManager,
) : LegendValueProvider {

    override fun provideLegendValue(dataElementUid: String, value: String?): LegendValue? {
        return value?.let {
            try {
                val dataElement = d2.dataElementModule().dataElements()
                    .byUid().eq(dataElementUid)
                    .withLegendSets()
                    .one().blockingGet()
                if (dataElement?.valueType()?.isNumeric == true &&
                    dataElement.legendSets()?.isNotEmpty() == true
                ) {
                    val legendSet = dataElement.legendSets()!![0]
                    val legend = d2.legendSetModule().legends()
                        .byStartValue().smallerThan(java.lang.Double.valueOf(value))
                        .byEndValue().biggerThan(java.lang.Double.valueOf(value))
                        .byLegendSet().eq(legendSet.uid())
                        .one()
                        .blockingGet() ?: d2.legendSetModule().legends()
                        .byEndValue().eq(java.lang.Double.valueOf(value))
                        .byLegendSet().eq(legendSet.uid())
                        .one()
                        .blockingGet()
                    if (legend != null) {
                        return LegendValue(
                            resourceManager.getColorFrom(legend.color()),
                            legend.displayName(),
                        )
                    }
                }
                null
            } catch (e: Exception) {
                null
            }
        }
    }
}
