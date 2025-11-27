package org.dhis2.mobile.commons.model.internal

data class ValueInfo(
    val optionSetUid: String?,
    val valueIsValidOption: Boolean,
    val isMultiText: Boolean,
    val isOrganisationUnit: Boolean,
    val isFile: Boolean,
    val isDate: Boolean,
    val isDateTime: Boolean,
    val isTime: Boolean,
    val isPercentage: Boolean,
    val valueIsAValidOrgUnit: Boolean,
    val valueIsAValidFile: Boolean,
    val isCoordinate: Boolean,
    val isBooleanType: Boolean,
) {
    fun parseToOptionName() = !isMultiText and valueIsValidOption

    fun parseToOrgUnitName() = isOrganisationUnit and valueIsAValidOrgUnit

    fun parseToFileName() = isFile and valueIsAValidFile
}
