package org.dhis2.usescases.reservedValue

import io.reactivex.processors.FlowableProcessor
import org.hisp.dhis.android.core.trackedentity.ReservedValueSummary

class ReservedValueMapper(
    private val refillProcessor: FlowableProcessor<String>,
    private val leftValueLabel: String,
) {
    fun map(reservedValueSummaries: List<ReservedValueSummary>): List<ReservedValueModel> {
        return reservedValueSummaries.map {
            ReservedValueModel(
                it.trackedEntityAttribute().uid(),
                it.trackedEntityAttribute().displayFormName() ?: "",
                it.organisationUnit()?.uid(),
                it.organisationUnit()?.displayName(),
                it.count(),
                leftValueLabel,
                refillProcessor,
            )
        }
    }
}
