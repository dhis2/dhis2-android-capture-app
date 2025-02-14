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
) {
    fun parseToOptionName() = !isMultiText and valueIsValidOption
    fun parseToOrgUnitName() = isOrganisationUnit and valueIsAValidOrgUnit
    fun parseToFilePath() = isFile and valueIsAValidFile
}
