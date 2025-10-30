package org.dhis2.mobile.aggregates.data.mappers

import org.dhis2.mobile.commons.input.InputType
import org.hisp.dhis.android.core.common.ValueType

internal fun ValueType.toInputType() =
    when (this) {
        ValueType.TEXT -> InputType.Text
        ValueType.LONG_TEXT -> InputType.LongText
        ValueType.LETTER -> InputType.Letter
        ValueType.PHONE_NUMBER -> InputType.PhoneNumber
        ValueType.EMAIL -> InputType.Email
        ValueType.BOOLEAN -> InputType.Boolean
        ValueType.TRUE_ONLY -> InputType.TrueOnly
        ValueType.DATE -> InputType.Date
        ValueType.DATETIME -> InputType.DateTime
        ValueType.TIME -> InputType.Time
        ValueType.NUMBER -> InputType.Number
        ValueType.UNIT_INTERVAL -> InputType.UnitInterval
        ValueType.PERCENTAGE -> InputType.Percentage
        ValueType.INTEGER -> InputType.Integer
        ValueType.INTEGER_POSITIVE -> InputType.IntegerPositive
        ValueType.INTEGER_NEGATIVE -> InputType.IntegerNegative
        ValueType.INTEGER_ZERO_OR_POSITIVE -> InputType.IntegerZeroOrPositive
        ValueType.TRACKER_ASSOCIATE -> InputType.TrackerAssociate
        ValueType.USERNAME -> InputType.Username
        ValueType.COORDINATE -> InputType.Coordinates
        ValueType.ORGANISATION_UNIT -> InputType.OrganisationUnit
        ValueType.REFERENCE -> InputType.Reference
        ValueType.AGE -> InputType.Age
        ValueType.URL -> InputType.Url
        ValueType.FILE_RESOURCE -> InputType.FileResource
        ValueType.IMAGE -> InputType.Image
        ValueType.GEOJSON -> InputType.GeoJson
        ValueType.MULTI_TEXT -> InputType.MultiText
    }
