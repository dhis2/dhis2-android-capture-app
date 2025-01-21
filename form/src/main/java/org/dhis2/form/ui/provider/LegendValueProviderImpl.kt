package org.dhis2.form.ui.provider

import androidx.compose.ui.graphics.Color
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.LegendValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.mobile.ui.designsystem.component.LegendDescriptionData

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
                    val legendValues = d2.legendSetModule()
                        .legendSets()
                        .withLegends()
                        .uid(legendSet.uid())
                        .blockingGet()
                    if (legend != null) {
                        return LegendValue(
                            resourceManager.getColorFrom(legend.color()),
                            legend.displayName(),
                            legendValues?.legends()?.sortedBy { it.startValue() }?.map {
                                LegendDescriptionData(
                                    Color(resourceManager.getColorFrom(it.color())),
                                    it.displayName() ?: "",
                                    IntRange(
                                        it.startValue()?.toInt() ?: 0,
                                        it.endValue()?.toInt() ?: 0,
                                    ),
                                )
                            },
                        )
                    }
                }
                null
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun hasLegendSet(dataElementUid: String): Boolean {
        return d2.dataElementModule().dataElements()
            .byUid().eq(dataElementUid)
            .withLegendSets()
            .one().blockingGet()
            ?.legendSets()?.isNotEmpty()
            ?: false
    }
}
