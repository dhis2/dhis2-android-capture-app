package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

fun TrackedEntityAttributeValue.userFriendlyValue(d2: D2): String? {
    val attribute = d2.trackedEntityModule()
        .trackedEntityAttributes().uid(trackedEntityAttribute())
        .blockingGet()

    if(value().isNullOrEmpty())
        return value()

    attribute.optionSet()?.let {
        val option = d2.optionModule().options()
            .byOptionSetUid().eq(it.uid())
            .byCode().eq(value()).one().blockingGet()
        return if(option!=null) {
            option.displayName()
        }else{
            "Invalid value"
        }
    } ?: return when (attribute.valueType()) {
        ValueType.ORGANISATION_UNIT -> d2.organisationUnitModule().organisationUnits()
            .uid(value()).blockingGet().displayName()
        else -> value()
    }
}