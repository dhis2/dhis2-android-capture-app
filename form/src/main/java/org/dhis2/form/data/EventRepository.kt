package org.dhis2.form.data

import android.text.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.bindings.blockingGetValueCheck
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.form.data.metadata.FormBaseConfiguration
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageSection

class EventRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val eventUid: String,
    private val d2: D2,
    private val metadataIconProvider: MetadataIconProvider,
) : DataEntryBaseRepository(FormBaseConfiguration(d2), fieldFactory) {

    private val event by lazy {
        d2.eventModule().events().uid(eventUid)
            .blockingGet()
    }

    override val programUid by lazy {
        event?.program()
    }

    private val sectionMap by lazy {
        d2.programModule().programStageSections()
            .byProgramStageUid().eq(event?.programStage())
            .withDataElements()
            .blockingGet()
            .map { section -> section.uid() to section }
            .toMap()
    }

    override fun sectionUids(): Flowable<List<String>> {
        return Flowable.just(sectionMap.keys.toList())
    }

    override fun list(): Flowable<List<FieldUiModel>> {
        return d2.programModule().programStageSections()
            .byProgramStageUid().eq(event?.programStage())
            .withDataElements()
            .get()
            .flatMap { programStageSection ->
                if (programStageSection.isEmpty()) {
                    getFieldsForSingleSection()
                } else {
                    getFieldsForMultipleSections()
                }
            }.map { list ->
                val fields = list.toMutableList()
                fields.add(fieldFactory.createClosingSection())
                fields.toList()
            }.toFlowable()
    }

    override fun isEvent(): Boolean {
        return true
    }

    override fun getSpecificDataEntryItems(uid: String): List<FieldUiModel> {
        // pending implementation in Event Form
        return emptyList()
    }

    private fun getFieldsForSingleSection(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val stageDataElements = d2.programModule().programStageDataElements().withRenderType()
                .byProgramStage().eq(event?.programStage())
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()

            stageDataElements.map { programStageDataElement ->
                transform(programStageDataElement)
            }
        }
    }

    private fun getFieldsForMultipleSections(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val fields = mutableListOf<FieldUiModel>()
            sectionMap.values.forEach { programStageSection ->
                fields.add(
                    transformSection(
                        programStageSection.uid(),
                        programStageSection.displayName(),
                        programStageSection.displayDescription(),
                    ),
                )
                programStageSection.dataElements()?.forEach { dataElement ->
                    d2.programModule().programStageDataElements().withRenderType()
                        .byProgramStage().eq(event?.programStage())
                        .byDataElement().eq(dataElement.uid())
                        .one().blockingGet()?.let {
                            fields.add(
                                transform(it),
                            )
                        }
                }
            }
            return@fromCallable fields
        }
    }

    private fun transform(programStageDataElement: ProgramStageDataElement): FieldUiModel {
        val de = d2.dataElementModule().dataElements().uid(
            programStageDataElement.dataElement()!!.uid(),
        ).blockingGet()
        val uid = de?.uid() ?: ""
        val displayName = de?.displayName() ?: ""
        val valueType = de?.valueType()
        val mandatory = programStageDataElement.compulsory() ?: false
        val optionSet = de?.optionSetUid()
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid)
        val programStageSection: ProgramStageSection? = sectionMap.values.firstOrNull { section ->
            section.dataElements()?.map { it.uid() }?.contains(de?.uid()) ?: false
        }
        var dataValue = when {
            valueRepository.blockingExists() -> valueRepository.blockingGet()?.value()
            else -> null
        }
        val friendlyValue = dataValue?.let {
            valueRepository.blockingGetValueCheck(d2, uid).userFriendlyValue(d2)
        }
        val allowFutureDates = programStageDataElement.allowFutureDate() ?: false
        val formName = de?.displayFormName()
        val description = de?.displayDescription()
        var optionSetConfig: OptionSetConfiguration? = null
        if (!TextUtils.isEmpty(optionSet)) {
            if (!TextUtils.isEmpty(dataValue) && d2.optionModule().options().byOptionSetUid()
                    .eq(optionSet).byCode()
                    .eq(dataValue)
                    .one().blockingExists()
            ) {
                dataValue =
                    d2.optionModule().options().byOptionSetUid().eq(optionSet)
                        .byCode()
                        .eq(dataValue).one().blockingGet()?.displayName()
            }
            val optionCount =
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .blockingCount()
            optionSetConfig = OptionSetConfiguration.config(optionCount) {
                val options = d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()

                val metadataIconMap = options.associate { it.uid() to metadataIconProvider(it.style()) }

                OptionSetConfiguration.OptionConfigData(
                    options = options,
                    metadataIconMap = metadataIconMap,
                )
            }
        }
        val fieldRendering = getValueTypeDeviceRendering(programStageDataElement)
        val objectStyle = getObjectStyle(de)

        var (error, warning) = de?.uid()?.let { deUid ->
            getConflictErrorsAndWarnings(deUid, dataValue)
        } ?: Pair(null, null)

        val isOrgUnit =
            valueType === ValueType.ORGANISATION_UNIT
        val isDate = valueType != null && valueType.isDate
        if (!isOrgUnit && !isDate) {
            dataValue = friendlyValue
        }
        val renderingType = getSectionRenderingType(programStageSection)
        val featureType = getFeatureType(valueType)

        var fieldViewModel = fieldFactory.create(
            uid,
            formName ?: displayName,
            valueType!!,
            mandatory,
            optionSet,
            dataValue,
            programStageSection?.uid(),
            allowFutureDates,
            isEventEditable(),
            renderingType,
            description,
            fieldRendering,
            objectStyle,
            de.fieldMask(),
            optionSetConfig,
            featureType,
        )

        if (!error.isNullOrEmpty()) {
            fieldViewModel = fieldViewModel.setError(error)
        }

        if (!warning.isNullOrEmpty()) {
            fieldViewModel = fieldViewModel.setWarning(warning)
        }

        return fieldViewModel
    }

    private fun getConflictErrorsAndWarnings(
        dataElementUid: String,
        dataValue: String?,
    ): Pair<String?, String?> {
        var error: String? = null
        var warning: String? = null

        val conflicts = d2.importModule().trackerImportConflicts()
            .byEventUid().eq(eventUid)
            .blockingGet()

        val conflict = conflicts
            .find { it.dataElement() == dataElementUid }

        when (conflict?.status()) {
            ImportStatus.WARNING -> warning = getError(conflict, dataValue)
            ImportStatus.ERROR -> error = getError(conflict, dataValue)
            else -> {}
        }

        return Pair(error, warning)
    }

    private fun getObjectStyle(de: DataElement?) = de?.style() ?: ObjectStyle.builder().build()

    private fun getValueTypeDeviceRendering(programStageDataElement: ProgramStageDataElement) =
        if (programStageDataElement.renderType() != null) {
            programStageDataElement.renderType()!!
                .mobile()
        } else {
            null
        }

    private fun getFeatureType(valueType: ValueType?) = when (valueType) {
        ValueType.COORDINATE -> FeatureType.POINT
        else -> null
    }

    private fun getSectionRenderingType(programStageSection: ProgramStageSection?) =
        programStageSection?.renderType()?.mobile()?.type()

    private fun isEventEditable() = d2.eventModule().eventService().blockingIsEditable(eventUid)

    private fun checkConflicts(dataElementUid: String): String {
        return d2.importModule().trackerImportConflicts()
            .byEventUid().eq(eventUid)
            .blockingGet()
            .firstOrNull { conflict ->
                conflict.event() == eventUid &&
                    conflict.dataElement() == dataElementUid
            }?.displayDescription() ?: ""
    }
}
