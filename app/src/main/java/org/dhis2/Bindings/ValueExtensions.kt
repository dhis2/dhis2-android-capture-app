package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

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

fun TrackedEntityDataValue.userFriendlyValue(d2: D2): String? {
    val de = d2.dataElementModule()
        .dataElements().uid(dataElement())
        .blockingGet()

    if(value().isNullOrEmpty())
        return value()

    de.optionSet()?.let {
        val option = d2.optionModule().options()
            .byOptionSetUid().eq(it.uid())
            .byCode().eq(value()).one().blockingGet()
        return if(option!=null) {
            option.displayName()
        }else{
            "Invalid value"
        }
    } ?: return when (de.valueType()) {
        ValueType.ORGANISATION_UNIT -> d2.organisationUnitModule().organisationUnits()
            .uid(value()).blockingGet().displayName()
        else -> value()
    }
}