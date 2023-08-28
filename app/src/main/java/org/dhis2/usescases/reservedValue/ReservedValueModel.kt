package org.dhis2.usescases.reservedValue

import io.reactivex.processors.FlowableProcessor

data class ReservedValueModel(
    val attributeUid: String,
    val attributeName: String,
    val orgUnitUid: String?,
    val orgUnitName: String?,
    val count: Int,
    val leftValuesLabel: String,
    val refillProcessor: FlowableProcessor<String>,
) {
    fun hasOrgUnit(): Boolean {
        return orgUnitUid != null
    }

    fun valuesLeft(): String {
        return leftValuesLabel.format(count)
    }

    fun refill() {
        refillProcessor.onNext(attributeUid)
    }
}
