package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository

fun TrackedEntityAttributeValue.userFriendlyValue(d2: D2): String? {
    if (value().isNullOrEmpty()) {
        return value()
    }

    val attribute = d2.trackedEntityModule().trackedEntityAttributes()
        .uid(trackedEntityAttribute())
        .blockingGet()

    if(attribute == null){
        return value()
    }

    if (check(d2, attribute.valueType(), attribute.optionSet()?.uid(), value()!!)) {
        attribute.optionSet()?.let {
            return checkOptionSetValue(d2, it.uid(), value()!!)
        } ?: return checkValueTypeValue(d2, attribute.valueType(), value()!!)
    } else {
        return null
    }
}

fun TrackedEntityDataValue?.userFriendlyValue(d2: D2): String? {
    if (this == null) {
        return null
    } else {
        if (value().isNullOrEmpty()) {
            return value()
        }

        val dataElement = d2.dataElementModule().dataElements()
            .uid(dataElement())
            .blockingGet()

        if (check(d2, dataElement.valueType(), dataElement.optionSet()?.uid(), value()!!)) {
            dataElement.optionSet()?.let {
                return checkOptionSetValue(d2, it.uid(), value()!!)
            } ?: return checkValueTypeValue(d2, dataElement.valueType(), value()!!)
        } else {
            return null
        }
    }
}

fun checkOptionSetValue(d2: D2, optionSetUid: String, code: String): String? {
    return d2.optionModule().options()
        .byOptionSetUid().eq(optionSetUid)
        .byCode().eq(code).one().blockingGet()?.displayName()
}

fun checkValueTypeValue(d2: D2, valueType: ValueType?, value: String): String {
    return when (valueType) {
        ValueType.ORGANISATION_UNIT ->
            d2.organisationUnitModule().organisationUnits()
                .uid(value)
                .blockingGet()
                .displayName()!!
        ValueType.IMAGE, ValueType.FILE_RESOURCE ->
            if (d2.fileResourceModule().fileResources().uid(value).blockingExists()) {
                d2.fileResourceModule().fileResources().uid(value).blockingGet().path()!!
            } else {
                ""
            }
        else -> value
    }
}

fun TrackedEntityAttributeValueObjectRepository.blockingSetCheck(
    d2: D2,
    attrUid: String,
    value: String
): Boolean {
    return d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet().let {
        if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
            val finalValue = assureCodeForOptionSet(d2, it.optionSet()?.uid(), value)
            blockingSet(finalValue)
            true
        } else {
            blockingDeleteIfExist()
            false
        }
    }
}

fun TrackedEntityAttributeValueObjectRepository.blockingGetCheck(
    d2: D2,
    attrUid: String
): TrackedEntityAttributeValue? {
    return d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet().let {
        if (blockingExists() && check(
            d2,
            it.valueType(),
            it.optionSet()?.uid(),
            blockingGet().value()!!
        )
        ) {
            blockingGet()
        } else {
            blockingDeleteIfExist()
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
            val finalValue = assureCodeForOptionSet(d2, it.optionSet()?.uid(), value)
            blockingSet(finalValue)
            true
        } else {
            blockingDeleteIfExist()
            false
        }
    }
}

fun TrackedEntityDataValueObjectRepository.blockingGetValueCheck(
    d2: D2,
    deUid: String
): TrackedEntityDataValue? {
    return d2.dataElementModule().dataElements().uid(deUid).blockingGet().let {
        if (blockingExists() && check(
            d2,
            it.valueType(),
            it.optionSet()?.uid(),
            blockingGet().value()!!
        )
        ) {
            blockingGet()
        } else {
            blockingDeleteIfExist()
            null
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
        optionSetUid != null -> {
            val optionByCodeExist = d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
                .byCode().eq(value).one().blockingExists()
            val optionByNameExist = d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
                .byDisplayName().eq(value).one().blockingExists()
            optionByCodeExist || optionByNameExist
        }
        valueType != null -> {
            if (valueType.isNumeric) {
                try {
                    value.toFloat().toString()
                    true
                } catch (e: Exception) {
                    false
                }
            } else {
                when (valueType) {
                    ValueType.FILE_RESOURCE, ValueType.IMAGE ->
                        d2.fileResourceModule().fileResources()
                            .byUid().eq(value).one().blockingExists()
                    ValueType.ORGANISATION_UNIT ->
                        d2.organisationUnitModule().organisationUnits().uid(value).blockingExists()
                    else -> true
                }
            }
        }
        else -> false
    }
}

private fun assureCodeForOptionSet(d2: D2, optionSetUid: String?, value: String): String? {
    return optionSetUid?.let {
        if (d2.optionModule().options()
            .byOptionSetUid().eq(it)
            .byName().eq(value)
            .one().blockingExists()
        ) {
            d2.optionModule().options().byOptionSetUid().eq(it).byName().eq(value).one()
                .blockingGet().code()
        } else {
            value
        }
    } ?: value
}
