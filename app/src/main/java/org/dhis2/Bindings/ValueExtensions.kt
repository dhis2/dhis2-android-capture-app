package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.CoordinateHelper
import org.hisp.dhis.android.core.common.Coordinates
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository

fun TrackedEntityAttributeValue.userFriendlyValue(d2: D2): String? {
    val attribute = d2.trackedEntityModule()
        .trackedEntityAttributes().uid(trackedEntityAttribute())
        .blockingGet()

    if (value().isNullOrEmpty())
        return value()

    attribute.optionSet()?.let {
        val option = d2.optionModule().options()
            .byOptionSetUid().eq(it.uid())
            .byCode().eq(value()).one().blockingGet()
        return if (option != null) {
            option.displayName()
        } else {
            "Invalid value"
        }
    } ?: return when (attribute.valueType()) {
        ValueType.ORGANISATION_UNIT -> d2.organisationUnitModule().organisationUnits()
            .uid(value()).blockingGet().displayName()
        else -> value()
    }
}

fun TrackedEntityAttributeValueObjectRepository.blockingSetCheck(
    d2: D2,
    attrUid: String,
    value: String
): Boolean {
    return d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet().let {
        if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
            blockingSet(value)
            true
        } else {
            blockingDelete()
            false
        }
    }
}

fun TrackedEntityAttributeValueObjectRepository.blockingGetValueCheck(
    d2: D2,
    attrUid: String,
    value: String
): String? {
    return d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet().let {
        if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
            blockingGet().value()
        } else {
            blockingDelete()
            null
        }
    }
}

fun TrackedEntityDataValueObjectRepository.blockingSetCheck(
    d2: D2,
    deUid: String,
    value: String
): Boolean {
    return d2.dataElementModule().dataElements().uid(deUid).blockingGet().let {
        if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
            blockingSet(value)
            true
        } else {
            blockingDelete()
            false
        }
    }
}

fun TrackedEntityDataValueObjectRepository.blockingGetCheck(
    d2: D2,
    deUid: String,
    value: String
): Boolean {
    return d2.dataElementModule().dataElements().uid(deUid).blockingGet().let {
        if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
            blockingSet(value)
            true
        } else {
            blockingDelete()
            false
        }
    }
}

private fun check(
    d2: D2,
    valueType: ValueType?,
    optionSetUid: String?,
    value: String
): Boolean {
    return when {
        optionSetUid != null ->
            d2.optionModule().options().byOptionSetUid().eq(optionSetUid).byCode().eq(value).one().blockingExists()
        valueType != null ->
            when (valueType) {
                ValueType.FILE_RESOURCE, ValueType.IMAGE ->
                    d2.fileResourceModule().fileResources().byUid().eq(value).one().blockingExists()
                ValueType.ORGANISATION_UNIT -> d2.organisationUnitModule().organisationUnits().uid(
                    value
                ).blockingExists()
                else -> true
            }
        else -> false
    }
}