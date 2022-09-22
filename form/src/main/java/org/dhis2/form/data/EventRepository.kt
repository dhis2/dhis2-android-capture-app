package org.dhis2.form.data

import android.text.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.Bindings.blockingGetValueCheck
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageSection

class EventRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val eventUid: String,
    private val d2: D2
) : DataEntryBaseRepository(d2, fieldFactory) {

    private val event by lazy {
        d2.eventModule().events().uid(eventUid)
            .blockingGet()
    }

    private val sectionMap by lazy {
        d2.programModule().programStageSections()
            .byProgramStageUid().eq(event.programStage())
            .withDataElements()
            .blockingGet()
            .map { section -> section.uid() to section }
            .toMap()
    }

    override fun sectionUids(): Flowable<MutableList<String>> {
        return Flowable.just(sectionMap.keys.toMutableList())
    }

    override fun list(): Flowable<MutableList<FieldUiModel>> {
        return d2.programModule().programStageSections()
            .byProgramStageUid().eq(event.programStage())
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
                fields
            }.toFlowable()
    }

    override fun isEvent(): Boolean {
        return true
    }

    private fun getFieldsForSingleSection(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val stageDataElements = d2.programModule().programStageDataElements().withRenderType()
                .byProgramStage().eq(event.programStage())
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
                        programStageSection.displayName()
                    )
                )
                programStageSection.dataElements()?.forEach { dataElement ->
                    d2.programModule().programStageDataElements().withRenderType()
                        .byProgramStage().eq(event.programStage())
                        .byDataElement().eq(dataElement.uid())
                        .one().blockingGet()?.let {
                        fields.add(
                            transform(it)
                        )
                    }
                }
            }
            return@fromCallable fields
        }
    }

    private fun transform(programStageDataElement: ProgramStageDataElement): FieldUiModel {
        val de = d2.dataElementModule().dataElements().uid(
            programStageDataElement.dataElement()!!.uid()
        ).blockingGet()
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, de.uid())
        var programStageSection: ProgramStageSection? = null
        for (section in sectionMap.values) {
            if (getUidsList<DataElement>(section.dataElements()!!).contains(de.uid())) {
                programStageSection = section
                break
            }
        }
        val uid = de.uid()
        val displayName = de.displayName()!!
        val valueType = de.valueType()
        val mandatory = programStageDataElement.compulsory() ?: false
        val optionSet = de.optionSetUid()
        var dataValue =
            if (valueRepository.blockingExists()) valueRepository.blockingGet()
                .value() else null
        val friendlyValue =
            if (dataValue != null) valueRepository.blockingGetValueCheck(d2, uid)
                .userFriendlyValue(d2) else null
        val allowFutureDates = programStageDataElement.allowFutureDate() ?: false
        val formName = de.displayFormName()
        val description = de.displayDescription()
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
                        .eq(dataValue).one().blockingGet().displayName()
            }
            val optionCount =
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .blockingCount()
            optionSetConfig = OptionSetConfiguration.config(
                optionCount
            ) {
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()
            }
        }
        val fieldRendering = getValueTypeDeviceRendering(programStageDataElement)
        val objectStyle = getObjectStyle(de)
        val error: String = checkConflicts(de.uid(), dataValue)
        val isOrgUnit =
            valueType === ValueType.ORGANISATION_UNIT
        val isDate = valueType != null && valueType.isDate
        if (!isOrgUnit && !isDate) {
            dataValue = friendlyValue
        }
        val renderingType = getSectionRenderingType(programStageSection)
        val featureType = getFeatureType(valueType)

        val fieldViewModel = fieldFactory.create(
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
            featureType
        )
        return if (error.isNotEmpty()) {
            fieldViewModel.setError(error)
        } else {
            fieldViewModel
        }
    }

    private fun getObjectStyle(de: DataElement) =
        if (de.style() != null) de.style() else ObjectStyle.builder().build()

    private fun getValueTypeDeviceRendering(programStageDataElement: ProgramStageDataElement) =
        if (programStageDataElement.renderType() != null) programStageDataElement.renderType()!!
            .mobile() else null

    private fun getFeatureType(valueType: ValueType?) =
        when (valueType) {
            ValueType.COORDINATE -> FeatureType.POINT
            else -> null
        }

    private fun getSectionRenderingType(programStageSection: ProgramStageSection?) =
        programStageSection?.renderType()?.mobile()?.type()

    private fun isEventEditable() = d2.eventModule().eventService().blockingIsEditable(eventUid)

    private fun checkConflicts(dataElementUid: String, value: String?): String {
        return d2.importModule().trackerImportConflicts()
            .byEventUid().eq(eventUid)
            .blockingGet()
            .firstOrNull { conflict ->
                conflict.event() == eventUid &&
                    conflict.dataElement() == dataElementUid &&
                    conflict.value() == value
            }?.displayDescription() ?: ""
    }
}
