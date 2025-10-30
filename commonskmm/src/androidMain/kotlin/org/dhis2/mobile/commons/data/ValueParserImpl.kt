package org.dhis2.mobile.commons.data

import org.dhis2.mobile.commons.model.internal.ValueInfo
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.no
import org.dhis2.mobile.commons.resources.yes
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.jetbrains.compose.resources.getString

internal class ValueParserImpl(
    private val d2: D2,
) : ValueParser {
    override suspend fun getValueInfo(
        uid: String,
        value: String,
    ) = getValueTypeAndOptionSetUid(uid).let { (valueType, optionSetUid) ->
        ValueInfo(
            optionSetUid = optionSetUid,
            valueIsValidOption = valueAsOptionExists(value, optionSetUid.orEmpty()),
            isMultiText = valueType == ValueType.MULTI_TEXT,
            isOrganisationUnit = valueType == ValueType.ORGANISATION_UNIT,
            valueIsAValidOrgUnit = valueAsOrgUnitExist(value),
            isFile = (valueType == ValueType.IMAGE) or (valueType == ValueType.FILE_RESOURCE),
            valueIsAValidFile = valueAsFileExists(value),
            isPercentage = valueType == ValueType.PERCENTAGE,
            isDate = (valueType == ValueType.DATE) or (valueType == ValueType.AGE),
            isDateTime = valueType == ValueType.DATETIME,
            isTime = valueType == ValueType.TIME,
            isCoordinate = valueType == ValueType.COORDINATE,
            isBooleanType = valueType == ValueType.BOOLEAN,
        )
    }

    override suspend fun valueFromMultiTextAsOptionNames(
        optionSetUid: String,
        value: String,
    ): String {
        val options =
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq(optionSetUid)
                .blockingGet()
                .associate { it.code() to it.displayName() }
        return value
            .split(",")
            .map { optionCode ->
                options[optionCode]
            }.joinToString(",")
    }

    override suspend fun valueFromOptionSetAsOptionName(
        optionSetUid: String,
        value: String,
    ) = d2
        .optionModule()
        .options()
        .byOptionSetUid()
        .eq(optionSetUid)
        .byCode()
        .eq(value)
        .one()
        .blockingGet()
        ?.displayName() ?: value

    override suspend fun valueFromCoordinateAsLatLong(value: String): String {
        val geometry =
            Geometry
                .builder()
                .coordinates(value)
                .type(FeatureType.POINT)
                .build()

        return GeometryHelper.getPoint(geometry).let {
            "Lat: ${it[1]}\nLong: ${it[0]}"
        }
    }

    override suspend fun valueFromBooleanType(value: String): String =
        if (value == "true") getString(Res.string.yes) else getString(Res.string.no)

    override suspend fun valueFromOrgUnitAsOrgUnitName(value: String) =
        d2
            .organisationUnitModule()
            .organisationUnits()
            .uid(value)
            .blockingGet()
            ?.displayName() ?: value

    override suspend fun valueToFileName(value: String) =
        d2
            .fileResourceModule()
            .fileResources()
            .uid(value)
            .blockingGet()
            ?.name() ?: value

    private fun getValueTypeAndOptionSetUid(uid: String) =
        trackedEntityAttribute(uid)
            ?.let { Pair(it.valueType(), it.optionSet()?.uid()) }
            ?: dataElement(uid)
                ?.let { Pair(it.valueType(), it.optionSet()?.uid()) }
            ?: Pair(null, null)

    private fun valueAsOptionExists(
        value: String,
        optionSetUid: String,
    ): Boolean {
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
        return optionByCodeExist || optionByNameExist
    }

    private fun trackedEntityAttribute(uid: String) =
        d2
            .trackedEntityModule()
            .trackedEntityAttributes()
            .uid(uid)
            .blockingGet()

    private fun dataElement(uid: String) =
        d2
            .dataElementModule()
            .dataElements()
            .uid(uid)
            .blockingGet()

    private fun valueAsFileExists(value: String): Boolean =
        d2
            .fileResourceModule()
            .fileResources()
            .byUid()
            .eq(value)
            .one()
            .blockingExists()

    private fun valueAsOrgUnitExist(value: String): Boolean =
        d2
            .organisationUnitModule()
            .organisationUnits()
            .uid(value)
            .blockingExists()
}
