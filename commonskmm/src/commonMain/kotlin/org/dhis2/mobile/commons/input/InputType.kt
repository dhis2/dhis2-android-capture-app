package org.dhis2.mobile.commons.input

sealed class InputType {
    data object OptionSet : InputType()

    data object Text : InputType()

    data object LongText : InputType()

    data object Letter : InputType()

    data object PhoneNumber : InputType()

    data object Email : InputType()

    data object Boolean : InputType()

    data object TrueOnly : InputType()

    data object DateTime : InputType()

    data object Date : InputType()

    data object Time : InputType()

    data object Number : InputType()

    data object UnitInterval : InputType()

    data object Percentage : InputType()

    data object Integer : InputType()

    data object IntegerPositive : InputType()

    data object IntegerNegative : InputType()

    data object IntegerZeroOrPositive : InputType()

    data object TrackerAssociate : InputType()

    data object Username : InputType()

    data object Coordinates : InputType()

    data object OrganisationUnit : InputType()

    data object Reference : InputType()

    data object Age : InputType()

    data object Url : InputType()

    data object FileResource : InputType()

    data object Image : InputType()

    data object GeoJson : InputType()

    data object MultiText : InputType()

    data object QRCode : InputType()

    data object Barcode : InputType()

    fun isText() =
        listOf(
            Text,
            LongText,
            Letter,
            Time,
            Url,
            PhoneNumber,
            Email,
        ).contains(this)

    fun isNumeric() =
        listOf(
            Integer,
            IntegerPositive,
            IntegerNegative,
            IntegerZeroOrPositive,
            Number,
            UnitInterval,
            Percentage,
        ).contains(this)

    fun isDate() =
        listOf(
            Date,
            DateTime,
            Age,
        ).contains(this)
}
