package org.dhis2.mobile.commons.data

import org.dhis2.mobile.commons.model.internal.ValueInfo
import org.hisp.dhis.android.core.common.ValueType

/**
 * Builds the [ValueInfo] of [value] for a field whose [valueType] and [optionSetUid] are
 * already known.
 */
internal suspend fun ValueParser.getValueInfo(
    valueType: ValueType?,
    optionSetUid: String?,
    value: String,
) = ValueInfo(
    optionSetUid = optionSetUid,
    valueIsValidOption = optionSetUid != null && valueAsOptionExists(optionSetUid, value),
    isMultiText = valueType == ValueType.MULTI_TEXT,
    isOrganisationUnit = valueType == ValueType.ORGANISATION_UNIT,
    valueIsAValidOrgUnit = valueAsOrgUnitExists(value),
    isFile = (valueType == ValueType.IMAGE) or (valueType == ValueType.FILE_RESOURCE),
    valueIsAValidFile = valueAsFileExists(value),
    isPercentage = valueType == ValueType.PERCENTAGE,
    isDate = (valueType == ValueType.DATE) or (valueType == ValueType.AGE),
    isDateTime = valueType == ValueType.DATETIME,
    isTime = valueType == ValueType.TIME,
    isCoordinate = valueType == ValueType.COORDINATE,
    isBooleanType = valueType == ValueType.BOOLEAN,
    isTrueOnlyType = valueType == ValueType.TRUE_ONLY,
)
