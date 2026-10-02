package org.dhis2.bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository

/**
 * Returns the value to prefill a form field with: option code → option name, org unit uid → name,
 * file uid → local path. Other value types are returned as stored. Returns null for invalid values.
 */
fun TrackedEntityAttributeValue.toFormDisplayValue(d2: D2): String? =
    when {
        value().isNullOrEmpty() -> value()
        else -> {
            val attribute =
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(trackedEntityAttribute())
                    .blockingGet()
            value()!!.toFormDisplayValue(
                d2,
                attribute?.valueType(),
                attribute?.optionSet()?.uid(),
            )
        }
    }

/**
 * @see TrackedEntityAttributeValue.toFormDisplayValue
 */
fun TrackedEntityDataValue?.toFormDisplayValue(d2: D2): String? =
    when {
        this == null -> null
        value().isNullOrEmpty() -> value()
        else -> {
            val dataElement =
                d2
                    .dataElementModule()
                    .dataElements()
                    .uid(dataElement())
                    .blockingGet()

            value()!!.toFormDisplayValue(
                d2,
                dataElement?.valueType(),
                dataElement?.optionSetUid(),
            )
        }
    }

private fun String.toFormDisplayValue(
    d2: D2,
    valueType: ValueType?,
    optionSetUid: String?,
): String? =
    when {
        valueType == null || !check(d2, valueType, optionSetUid, this) -> null
        optionSetUid != null && valueType != ValueType.MULTI_TEXT -> checkOptionSetValue(d2, optionSetUid, this)
        valueType == ValueType.ORGANISATION_UNIT ->
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid(this)
                .blockingGet()
                ?.displayName() ?: this

        valueType == ValueType.IMAGE || valueType == ValueType.FILE_RESOURCE ->
            d2
                .fileResourceModule()
                .fileResources()
                .uid(this)
                .blockingGet()
                ?.path() ?: FILE_NOT_FOUND

        else -> this
    }

fun checkOptionSetValue(
    d2: D2,
    optionSetUid: String,
    code: String,
): String? =
    d2
        .optionModule()
        .options()
        .byOptionSetUid()
        .eq(optionSetUid)
        .byCode()
        .eq(code)
        .one()
        .blockingGet()
        ?.displayName()

fun TrackedEntityAttributeValueObjectRepository.blockingSetCheck(
    d2: D2,
    attrUid: String,
    value: String,
    onCrash: (attrUid: String, value: String) -> Unit = { _, _ -> },
): Boolean {
    return d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet()?.let {
        if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
            val finalValue = assureCodeForOptionSet(d2, it.optionSet()?.uid(), value)
            try {
                blockingSet(finalValue)
            } catch (e: Exception) {
                onCrash(attrUid, value)
                return false
            }
            true
        } else {
            blockingDeleteIfExist()
            false
        }
    } ?: false
}

fun TrackedEntityAttributeValueObjectRepository.blockingGetCheck(
    d2: D2,
    attrUid: String,
): TrackedEntityAttributeValue? =
    d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet()?.let {
        if (blockingExists() &&
            check(
                d2,
                it.valueType(),
                it.optionSet()?.uid(),
                blockingGet()?.value()!!,
            )
        ) {
            blockingGet()
        } else {
            blockingDeleteIfExist()
            null
        }
    }

fun TrackedEntityDataValueObjectRepository.blockingSetCheck(
    d2: D2,
    deUid: String,
    value: String,
): Boolean =
    d2.dataElementModule().dataElements().uid(deUid).blockingGet()?.let {
        if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
            val finalValue = assureCodeForOptionSet(d2, it.optionSet()?.uid(), value)
            blockingSet(finalValue)
            true
        } else {
            blockingDeleteIfExist()
            false
        }
    } ?: false

fun String?.withValueTypeCheck(valueType: ValueType?): String? {
    return this?.let {
        if (isEmpty()) return this
        when (valueType) {
            ValueType.PERCENTAGE,
            ValueType.INTEGER,
            ValueType.INTEGER_POSITIVE,
            ValueType.INTEGER_NEGATIVE,
            ValueType.INTEGER_ZERO_OR_POSITIVE,
            ->
                (
                    it.toIntOrNull() ?: it.toFloat().toInt()
                ).toString()

            ValueType.UNIT_INTERVAL -> (it.toIntOrNull() ?: it.toFloat()).toString()
            else -> this
        }
    } ?: this
}

fun TrackedEntityDataValueObjectRepository.blockingGetValueCheck(
    d2: D2,
    deUid: String,
): TrackedEntityDataValue? =
    d2.dataElementModule().dataElements().uid(deUid).blockingGet()?.let {
        if (blockingExists() &&
            check(
                d2,
                it.valueType(),
                it.optionSet()?.uid(),
                blockingGet()?.value()!!,
            )
        ) {
            blockingGet()
        } else if (it.valueType()?.isFile == true) {
            null
        } else {
            blockingDeleteIfExist()
            null
        }
    }

private fun check(
    d2: D2,
    valueType: ValueType?,
    optionSetUid: String?,
    value: String,
): Boolean =
    when {
        valueType != ValueType.MULTI_TEXT && optionSetUid != null -> {
            val optionByCodeExist =
                d2
                    .optionModule()
                    .options()
                    .byOptionSetUid()
                    .eq(optionSetUid)
                    .byCode()
                    .eq(value)
                    .one()
                    .blockingExists()
            val optionByNameExist =
                d2
                    .optionModule()
                    .options()
                    .byOptionSetUid()
                    .eq(optionSetUid)
                    .byDisplayName()
                    .eq(value)
                    .one()
                    .blockingExists()
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
                    ValueType.FILE_RESOURCE ->
                        d2
                            .fileResourceModule()
                            .fileResources()
                            .byUid()
                            .eq(value)
                            .one()
                            .blockingExists()

                    ValueType.ORGANISATION_UNIT ->
                        d2
                            .organisationUnitModule()
                            .organisationUnits()
                            .uid(value)
                            .blockingExists()

                    else -> true
                }
            }
        }

        else -> false
    }

private fun assureCodeForOptionSet(
    d2: D2,
    optionSetUid: String?,
    value: String,
): String =
    optionSetUid?.let {
        if (d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq(it)
                .byName()
                .eq(value)
                .one()
                .blockingExists()
        ) {
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq(it)
                .byName()
                .eq(value)
                .one()
                .blockingGet()
                ?.code()
        } else {
            value
        }
    } ?: value

const val FILE_NOT_FOUND = "fileNotFound"
