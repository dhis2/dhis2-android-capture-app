package org.dhis2.tracker.search.data

import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.search.model.SearchOperator
import org.dhis2.tracker.search.model.SearchParameterModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.arch.repositories.scope.internal.TrackerSearchOperator
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute

class SearchParametersRepositoryImpl(
    val d2: D2,
    val customIntentRepository: CustomIntentRepository,
    val domainErrorMapper: DomainErrorMapper,
) : SearchParametersRepository {
    override suspend fun getSearchParametersByProgram(programUid: String): List<SearchParameterModel> =
        try {
            // this is the attribute and its properties related to a specific program
            val programTrackedEntityAttributes =
                getSearchableProgramTrackedEntityAttributes(programUid)
            programTrackedEntityAttributes.mapNotNull { programTrackedEntityAttribute ->

                // Now, we get the TrackedEntityAttribute with all the info
                val trackedEntityAttribute =
                    getTrackedEntityAttributeByProgram(programTrackedEntityAttribute)
                trackedEntityAttribute?.let { attribute ->
                    val renderingType = programTrackedEntityAttribute.renderType()?.mobile()?.type()
                    val customIntent =
                        customIntentRepository.getCustomIntent(
                            attribute.uid(),
                            orgUnitUid = null,
                            actionType = CustomIntentActionTypeModel.SEARCH,
                        )

                    SearchParameterModel(
                        uid = attribute.uid(),
                        label = attribute.displayFormName() ?: "",
                        inputType =
                            getInputType(
                                attribute = attribute,
                                isCustomIntent = customIntent != null,
                                valueTypeRenderingType = renderingType,
                            ),
                        optionSet = attribute.optionSet()?.uid(),
                        customIntentUid = customIntent?.uid,
                        minCharactersToSearch = attribute.minCharactersToSearch(),
                        searchOperator = getSearchOperator(attribute),
                        isUnique = attribute.unique() ?: false,
                    )
                }
            }
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
        }

    override suspend fun getSearchParametersByTrackedEntityType(trackedEntityTypeUid: String): List<SearchParameterModel> =
        try {
            val trackedEntityAttributes =
                getTrackedEntityAttributesByTrackedEntityType(trackedEntityTypeUid)
            trackedEntityAttributes.mapNotNull { trackedEntityTypeAttribute ->
                // Now, we get the TrackedEntityAttribute with all the info
                val trackedEntityAttribute =
                    getTrackedEntityAttributeByTrackedEntityType(trackedEntityTypeAttribute)
                trackedEntityAttribute?.let { attribute ->
                    SearchParameterModel(
                        uid = attribute.uid(),
                        label = attribute.displayFormName() ?: "",
                        inputType =
                            getInputType(
                                attribute = attribute,
                                isCustomIntent = false,
                                valueTypeRenderingType = null,
                            ),
                        optionSet = attribute.optionSet()?.uid(),
                        customIntentUid = null,
                        minCharactersToSearch = attribute.minCharactersToSearch(),
                        searchOperator = getSearchOperator(attribute),
                        isUnique = attribute.unique() ?: false,
                    )
                }
            }
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
        }

    private fun getTrackedEntityAttributeByTrackedEntityType(
        trackedEntityTypeAttribute: TrackedEntityTypeAttribute,
    ): TrackedEntityAttribute? =
        trackedEntityTypeAttribute.trackedEntityAttribute()?.uid()?.let { uid ->
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid(uid)
                .blockingGet()
        }

    private fun getTrackedEntityAttributesByTrackedEntityType(trackedEntityTypeUid: String): List<TrackedEntityTypeAttribute> =
        d2
            .trackedEntityModule()
            .trackedEntityTypeAttributes()
            .byTrackedEntityTypeUid()
            .eq(trackedEntityTypeUid)
            .bySearchable()
            .isTrue
            .blockingGet()

    private fun getTrackedEntityAttributeByProgram(programTrackedEntityAttribute: ProgramTrackedEntityAttribute): TrackedEntityAttribute? =
        programTrackedEntityAttribute.trackedEntityAttribute()?.uid()?.let { uid ->
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid(uid)
                .blockingGet()
        }

    private fun getSearchableProgramTrackedEntityAttributes(programUid: String): List<ProgramTrackedEntityAttribute> =
        d2
            .programModule()
            .programTrackedEntityAttributes()
            .withRenderType()
            .byProgram()
            .eq(programUid)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
            .filter { programAttribute ->
                val isSearchable = programAttribute.searchable()!!
                val isUnique =
                    d2
                        .trackedEntityModule()
                        .trackedEntityAttributes()
                        .uid(programAttribute.trackedEntityAttribute()!!.uid())
                        .blockingGet()
                        ?.unique() == true
                isSearchable || isUnique
            }

    private fun getInputType(
        attribute: TrackedEntityAttribute,
        isCustomIntent: Boolean,
        valueTypeRenderingType: ValueTypeRenderingType?,
    ): TrackerInputType =
        when {
            attribute.optionSet()?.uid() != null && attribute.valueType() != ValueType.MULTI_TEXT
            -> getInputTypeForOptionSetByRenderingType(valueTypeRenderingType)

            isCustomIntent -> TrackerInputType.CUSTOM_INTENT

            else -> getInputTypeByValueType(attribute.valueType(), valueTypeRenderingType)
        }

    private fun getInputTypeByValueType(
        valueType: ValueType?,
        valueTypeRenderingType: ValueTypeRenderingType?,
    ): TrackerInputType =
        when (valueType) {
            ValueType.TEXT -> {
                when (valueTypeRenderingType) {
                    ValueTypeRenderingType.QR_CODE, ValueTypeRenderingType.GS1_DATAMATRIX -> TrackerInputType.QR_CODE
                    ValueTypeRenderingType.BAR_CODE -> TrackerInputType.BAR_CODE
                    else -> TrackerInputType.TEXT
                }
            }

            ValueType.INTEGER_POSITIVE -> TrackerInputType.INTEGER_POSITIVE
            ValueType.INTEGER_ZERO_OR_POSITIVE -> TrackerInputType.INTEGER_ZERO_OR_POSITIVE
            ValueType.PERCENTAGE -> TrackerInputType.PERCENTAGE
            ValueType.NUMBER -> TrackerInputType.NUMBER
            ValueType.INTEGER_NEGATIVE -> TrackerInputType.INTEGER_NEGATIVE
            ValueType.LONG_TEXT -> TrackerInputType.LONG_TEXT
            ValueType.INTEGER -> TrackerInputType.INTEGER
            ValueType.ORGANISATION_UNIT -> TrackerInputType.ORGANISATION_UNIT
            ValueType.EMAIL -> TrackerInputType.EMAIL
            ValueType.BOOLEAN -> {
                when (valueTypeRenderingType) {
                    ValueTypeRenderingType.HORIZONTAL_CHECKBOXES -> TrackerInputType.HORIZONTAL_CHECKBOXES
                    ValueTypeRenderingType.VERTICAL_CHECKBOXES -> TrackerInputType.VERTICAL_CHECKBOXES
                    ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS -> TrackerInputType.HORIZONTAL_RADIOBUTTONS
                    else -> TrackerInputType.VERTICAL_RADIOBUTTONS
                }
            }

            ValueType.TRUE_ONLY -> {
                when (valueTypeRenderingType) {
                    ValueTypeRenderingType.TOGGLE -> TrackerInputType.YES_ONLY_SWITCH
                    else -> TrackerInputType.YES_ONLY_CHECKBOX
                }
            }

            ValueType.PHONE_NUMBER -> TrackerInputType.PHONE_NUMBER
            ValueType.DATE -> TrackerInputType.DATE
            ValueType.DATETIME -> TrackerInputType.DATE_TIME
            ValueType.TIME -> TrackerInputType.TIME

            ValueType.AGE -> TrackerInputType.AGE
            ValueType.MULTI_TEXT -> TrackerInputType.MULTI_SELECTION

            ValueType.USERNAME,
            ValueType.LETTER,
            ValueType.UNIT_INTERVAL,
            ValueType.TRACKER_ASSOCIATE,
            ValueType.REFERENCE,
            ValueType.FILE_RESOURCE,
            ValueType.GEOJSON,
            ValueType.URL,
            null,
            -> TrackerInputType.NOT_SUPPORTED

            ValueType.COORDINATE -> TrackerInputType.COORDINATES
            ValueType.IMAGE -> TrackerInputType.IMAGE
        }

    private fun getInputTypeForOptionSetByRenderingType(valueTypeRenderingType: ValueTypeRenderingType?): TrackerInputType =
        when (valueTypeRenderingType) {
            ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS -> TrackerInputType.HORIZONTAL_RADIOBUTTONS
            ValueTypeRenderingType.VERTICAL_RADIOBUTTONS -> TrackerInputType.VERTICAL_RADIOBUTTONS
            ValueTypeRenderingType.HORIZONTAL_CHECKBOXES -> TrackerInputType.HORIZONTAL_CHECKBOXES
            ValueTypeRenderingType.VERTICAL_CHECKBOXES -> TrackerInputType.VERTICAL_CHECKBOXES
            else -> TrackerInputType.DROPDOWN
        }

    private fun getSearchOperator(attribute: TrackedEntityAttribute): SearchOperator? {
        val mainOperators = listOf(SearchOperator.LIKE, SearchOperator.SW, SearchOperator.EQ)
        val blockedOperators =
            attribute
                .blockedSearchOperators()
                ?.mapNotNull { sdkOperator ->
                    sdkOperator.toSearchOperator()
                } ?: emptyList()
        val preferredOperator: SearchOperator? =
            attribute.preferredSearchOperator()?.toSearchOperator()
        val valueType = attribute.valueType()
        val hasOptionSet = attribute.optionSet() != null
        val isUnique = attribute.unique() == true

        val alwaysEqValueTypes =
            listOf(
                ValueType.BOOLEAN,
                ValueType.TRUE_ONLY,
                ValueType.AGE,
                ValueType.ORGANISATION_UNIT,
            )

        val preferredOperatorValueTypes =
            listOf(
                ValueType.NUMBER,
                ValueType.INTEGER,
                ValueType.INTEGER_POSITIVE,
                ValueType.INTEGER_NEGATIVE,
                ValueType.INTEGER_ZERO_OR_POSITIVE,
                ValueType.DATE,
                ValueType.DATETIME,
                ValueType.TIME,
                ValueType.TEXT,
                ValueType.LONG_TEXT,
                ValueType.EMAIL,
                ValueType.PHONE_NUMBER,
                ValueType.PERCENTAGE,
            )

        return when {
            isUnique ||
                (hasOptionSet && valueType != ValueType.MULTI_TEXT) ||
                valueType in alwaysEqValueTypes -> {
                SearchOperator.EQ
            }

            valueType == ValueType.MULTI_TEXT -> {
                mainOperators.firstOrNull { it !in blockedOperators }
            }

            valueType in preferredOperatorValueTypes -> {
                if (preferredOperator != null && preferredOperator !in blockedOperators) {
                    preferredOperator
                } else {
                    mainOperators.firstOrNull { it !in blockedOperators }
                }
            }

            else -> null
        }
    }

    private fun TrackerSearchOperator.toSearchOperator() =
        when (this) {
            TrackerSearchOperator.LIKE -> SearchOperator.LIKE
            TrackerSearchOperator.SW -> SearchOperator.SW
            TrackerSearchOperator.EW -> SearchOperator.EW
            TrackerSearchOperator.EQ -> SearchOperator.EQ
        }
}
