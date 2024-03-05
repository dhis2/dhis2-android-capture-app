package org.dhis2.form.data

import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.form.data.metadata.EnrollmentConfiguration
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.SectionUiModelImpl.Companion.SINGLE_SECTION_UID
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.dhis2.form.ui.provider.inputfield.DEFAULT_MAX_DATE
import org.dhis2.form.ui.provider.inputfield.DEFAULT_MIN_DATE
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.program.ProgramSection
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import timber.log.Timber
import java.util.Date

class EnrollmentRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val conf: EnrollmentConfiguration,
    private val enrollmentMode: EnrollmentMode,
    private val enrollmentFormLabelsProvider: EnrollmentFormLabelsProvider,
) : DataEntryBaseRepository(conf, fieldFactory) {

    override val programUid by lazy {
        conf.program()?.uid()
    }

    private val programSections by lazy {
        conf.sections()
    }

    private fun canBeEdited(): Boolean {
        val selectedProgram = conf.program()
        val programAccess = selectedProgram?.access()?.data()?.write() == true
        val teTypeAccess = conf.trackedEntityType()?.access()?.data()?.write() == true
        return programAccess && teTypeAccess
    }
    override fun getSpecificDataEntryItems(uid: String): List<FieldUiModel> {
        return when (uid) {
            ORG_UNIT_UID -> {
                getEnrollmentData()
            }
            else -> {
                emptyList()
            }
        }
    }

    override fun sectionUids(): Flowable<List<String>> {
        val sectionUids = mutableListOf(ENROLLMENT_DATA_SECTION_UID)
        if (programSections.isEmpty()) {
            sectionUids.add(SINGLE_SECTION_UID)
        } else {
            sectionUids.addAll(programSections.map { it.uid() })
        }
        return Flowable.just(sectionUids)
    }

    override fun list(): Flowable<List<FieldUiModel>> {
        return Single.just(conf.sections())
            .flatMap { programSections ->
                if (programSections.isEmpty()) {
                    getFieldsForSingleSection()
                        .map { singleSectionList ->
                            val list = getSingleSectionList()
                            list.addAll(singleSectionList)
                            list
                        }
                } else {
                    getFieldsForMultipleSections()
                }
            }.map { list ->
                val fields = getEnrollmentData()
                fields.addAll(list)
                fields.add(fieldFactory.createClosingSection())
                fields.toList()
            }
            .toFlowable()
    }

    override fun isEvent(): Boolean {
        return false
    }

    private fun getSingleSectionList(): MutableList<FieldUiModel> {
        val teiType = conf.trackedEntityType()
        return mutableListOf(
            fieldFactory.createSingleSection(
                String.format(
                    enrollmentFormLabelsProvider.provideSingleSectionLabel(),
                    teiType?.displayName(),
                ),
            ),
        )
    }

    private fun getFieldsForSingleSection(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            conf.programAttributes().map { programTrackedEntityAttribute ->
                transform(programTrackedEntityAttribute)
            }
        }
    }

    private fun getFieldsForMultipleSections(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val fields = mutableListOf<FieldUiModel>()
            programSections.forEach { section ->
                fields.add(
                    transformSection(section.uid(), section.displayName(), section.description()),
                )
                section.attributes()?.forEachIndexed { _, attribute ->
                    conf.programAttribute(attribute.uid())?.let { programTrackedEntityAttribute ->
                        fields.add(transform(programTrackedEntityAttribute, section.uid()))
                    }
                }
            }
            return@fromCallable fields
        }
    }

    private fun transform(
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute,
        sectionUid: String? = SINGLE_SECTION_UID,
    ): FieldUiModel {
        val attribute = programTrackedEntityAttribute.trackedEntityAttribute()?.uid()?.let {
            conf.trackedEntityAttribute(it)
        } ?: throw IllegalStateException(
            "Attribute %s does not exist".format(
                programTrackedEntityAttribute.trackedEntityAttribute()?.uid(),
            ),
        )

        val valueType = attribute.valueType()
        var mandatory = programTrackedEntityAttribute.mandatory() ?: false
        val optionSet = attribute.optionSet()?.uid()
        val generated = attribute.generated() ?: false

        val orgUnitUid = conf.enrollment()
            ?.organisationUnit()

        var dataValue: String? = attribute.uid()
            ?.let { conf.attributeValue(it) }

        var optionSetConfig: OptionSetConfiguration? = null
        if (!optionSet.isNullOrEmpty()) {
            optionSetConfig = conf.optionSetConfig(optionSet)
        }

        var (error, warning) = getConflictErrorsAndWarnings(attribute.uid(), dataValue)

        if (generated && dataValue == null) {
            mandatory = true
            val result = handleAutogeneratedValue(attribute, orgUnitUid!!)
            dataValue = result.first
            warning = result.second
            if (!dataValue.isNullOrEmpty()) {
                conf.setValue(attribute.uid(), dataValue)
            }
        }

        if ((valueType == ValueType.ORGANISATION_UNIT || valueType?.isDate == true) &&
            !dataValue.isNullOrEmpty()
        ) {
            dataValue = conf.getValue(attribute.uid())?.value()
        }

        var programSection: ProgramSection? = null
        for (section in programSections) {
            if (getUidsList(section.attributes()!!).contains(attribute.uid())) {
                programSection = section
                break
            }
        }

        val renderingType = getSectionRenderingType(programSection)

        var fieldViewModel = fieldFactory.create(
            attribute.uid(),
            attribute.displayFormName() ?: "",
            valueType!!,
            mandatory,
            optionSet,
            dataValue,
            sectionUid,
            programTrackedEntityAttribute.allowFutureDate() ?: false,
            isEditable(generated),
            renderingType,
            attribute.displayDescription(),
            programTrackedEntityAttribute.renderType()?.mobile(),
            attribute.style(),
            attribute.fieldMask(),
            optionSetConfig,
            if (valueType == ValueType.COORDINATE) FeatureType.POINT else null,
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
        attributeUid: String,
        dataValue: String?,
    ): Pair<String?, String?> {
        var error: String? = null
        var warning: String? = null

        val conflicts = conf.conflicts()

        val conflict = conflicts
            .find { it.trackedEntityAttribute() == attributeUid }

        when (conflict?.status()) {
            ImportStatus.WARNING -> warning = getError(conflict, dataValue)
            ImportStatus.ERROR -> error = getError(conflict, dataValue)
            else -> {}
        }

        return Pair(error, warning)
    }

    private fun isEditable(generated: Boolean) = !generated && canBeEdited()

    private fun getSectionRenderingType(programSection: ProgramSection?) =
        programSection?.renderType()?.mobile()?.type()

    private fun handleAutogeneratedValue(
        attr: TrackedEntityAttribute,
        orgUnitUid: String,
    ): Pair<String?, String?> {
        var warning: String? = null
        var dataValue: String? = null
        try {
            val teiUid = conf.tei()

            if (teiUid != null) {
                try {
                    dataValue = conf.fetchAutogeneratedValue(attr.uid(), orgUnitUid)
                } catch (e: Exception) {
                    dataValue = null
                    warning = enrollmentFormLabelsProvider.provideReservedValueWarning()
                }

                if (attr.valueType() == ValueType.NUMBER) {
                    while (dataValue!!.startsWith("0")) {
                        dataValue = conf.fetchAutogeneratedValue(attr.uid(), orgUnitUid)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            warning = enrollmentFormLabelsProvider.provideReservedValueWarning()
        }

        return Pair(dataValue, warning)
    }

    private fun getEnrollmentData(): MutableList<FieldUiModel> {
        val enrollmentDataList = ArrayList<FieldUiModel>()
        enrollmentDataList.add(getEnrollmentDataSection(conf.program()?.displayName(), conf.program()?.description()))

        enrollmentDataList.add(
            getEnrollmentDateField(
                conf.program()?.enrollmentDateLabel()
                    ?: enrollmentFormLabelsProvider.provideEnrollmentDateDefaultLabel(programUid!!),
                conf.program()?.selectEnrollmentDatesInFuture(),
            ),
        )
        if (conf.program()?.displayIncidentDate()!!) {
            enrollmentDataList.add(
                getIncidentDateField(
                    conf.program()?.incidentDateLabel()
                        ?: enrollmentFormLabelsProvider.provideIncidentDateDefaultLabel(),
                    conf.program()?.selectIncidentDatesInFuture(),
                ),
            )
        }

        val orgUnits = conf.captureOrgUnitsCount()
        enrollmentDataList.add(
            getOrgUnitField(enrollmentMode == EnrollmentMode.NEW && orgUnits > 1),
        )

        val teiType = conf.trackedEntityType()
        if (teiType!!.featureType() != null && teiType.featureType() != FeatureType.NONE) {
            enrollmentDataList.add(getTeiCoordinatesField(teiType.featureType()))
        }

        if (conf.program()?.featureType() != null && conf.program()
                ?.featureType() != FeatureType.NONE
        ) {
            enrollmentDataList.add(
                getEnrollmentCoordinatesField(
                    conf.program()?.featureType(),
                ),
            )
        }

        return enrollmentDataList
    }

    private fun getAllowedDatesForEnrollmentDate(): SelectableDates {
        val selectedOrgUnit = conf.enrollment()?.organisationUnit()?.let { conf.orgUnit(it) }
        var minDate: Date? = null
        var maxDate: Date? = null
        val sdf = DateUtils.uiLibraryFormat()

        val selectedProgram = conf.program()
        if (selectedOrgUnit?.openingDate() != null) minDate = selectedOrgUnit.openingDate()
        when {
            (selectedOrgUnit?.closedDate() == null && java.lang.Boolean.FALSE == selectedProgram?.selectEnrollmentDatesInFuture()) -> {
                maxDate = Date(System.currentTimeMillis())
            }
            (selectedOrgUnit?.closedDate() != null && java.lang.Boolean.FALSE == selectedProgram?.selectEnrollmentDatesInFuture()) -> {
                maxDate = if (selectedOrgUnit.closedDate()!!.before(Date(System.currentTimeMillis()))) {
                    selectedOrgUnit.closedDate()
                } else {
                    Date(System.currentTimeMillis())
                }
            }

            (selectedOrgUnit?.closedDate() != null && java.lang.Boolean.TRUE == selectedProgram?.selectEnrollmentDatesInFuture()) -> {
                maxDate = selectedOrgUnit.closedDate()
            }
        }

        val maxDateString = if (maxDate != null) sdf.format(maxDate) else DEFAULT_MAX_DATE
        val minDateString = if (minDate != null) sdf.format(minDate) else DEFAULT_MIN_DATE
        return SelectableDates(minDateString, maxDateString)
    }

    private fun getEnrollmentDataSection(displayName: String?, description: String?): FieldUiModel {
        return fieldFactory.createSection(
            ENROLLMENT_DATA_SECTION_UID,
            displayName,
            description,
            true,
            0,
            0,
            SectionRenderingType.LISTING.name,
        )
    }

    private fun getEnrollmentDateField(
        enrollmentDateLabel: String,
        allowFutureDates: Boolean?,
    ): FieldUiModel {
        return fieldFactory.create(
            ENROLLMENT_DATE_UID,
            enrollmentDateLabel,
            ValueType.DATE,
            true, // check in constructor of dateViewModel
            null,
            when (val date = conf.enrollment()?.enrollmentDate()) {
                null -> null
                else -> DateUtils.oldUiDateFormat().format(date)
            },
            ENROLLMENT_DATA_SECTION_UID,
            allowFutureDates,
            canBeEdited(),
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            null,
            selectableDates = getAllowedDatesForEnrollmentDate(),
        )
    }

    private fun getIncidentDateField(
        incidentDateLabel: String,
        allowFutureDates: Boolean?,
    ): FieldUiModel {
        return fieldFactory.create(
            INCIDENT_DATE_UID,
            incidentDateLabel,
            ValueType.DATE,
            true,
            null,
            when (val date = conf.enrollment()?.incidentDate()) {
                null -> null
                else -> DateUtils.oldUiDateFormat().format(date)
            },
            ENROLLMENT_DATA_SECTION_UID,
            allowFutureDates,
            canBeEdited(),
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            null,
        )
    }

    private fun getOrgUnitField(editable: Boolean): FieldUiModel {
        return fieldFactory.create(
            ORG_UNIT_UID,
            enrollmentFormLabelsProvider.provideEnrollmentOrgUnitLabel(),
            ValueType.ORGANISATION_UNIT,
            true,
            null,
            conf.enrollment()?.organisationUnit(),
            ENROLLMENT_DATA_SECTION_UID,
            null,
            editable,
            SectionRenderingType.LISTING,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            null,
            orgUnitSelectorScope = programUid?.let { OrgUnitSelectorScope.ProgramCaptureScope(it) },
        )
    }

    private fun getTeiCoordinatesField(featureType: FeatureType?): FieldUiModel {
        val tei = conf.tei()
        val teiType = conf.trackedEntityType()
        val teiCoordinatesLabel = enrollmentFormLabelsProvider.provideTeiCoordinatesLabel()
        return fieldFactory.create(
            TEI_COORDINATES_UID,
            "$teiCoordinatesLabel ${teiType?.displayName()}",
            ValueType.COORDINATE,
            false,
            null,
            tei?.geometry()?.coordinates(),
            ENROLLMENT_DATA_SECTION_UID,
            null,
            canBeEdited(),
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            featureType,
        )
    }

    private fun getEnrollmentCoordinatesField(featureType: FeatureType?): FieldUiModel {
        return fieldFactory.create(
            ENROLLMENT_COORDINATES_UID,
            enrollmentFormLabelsProvider.provideEnrollmentCoordinatesLabel(programUid!!),
            ValueType.COORDINATE,
            false,
            null,
            conf.enrollment()?.geometry()?.coordinates(),
            ENROLLMENT_DATA_SECTION_UID,
            null,
            canBeEdited(),
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            featureType,
        )
    }

    fun hasEventsGeneratedByEnrollmentDate(): Boolean {
        return conf.hasEventsGeneratedByEnrollmentDate()
    }

    fun hasEventsGeneratedByIncidentDate(): Boolean {
        return conf.hasEventsGeneratedByIncidentDate()
    }

    override fun firstSectionToOpen(): String? {
        return if (enrollmentMode == EnrollmentMode.CHECK && isEnrollmentDataCompleted()) {
            sectionUids().blockingFirst().filterIndexed { index, _ -> index != 0 }.firstOrNull()
        } else {
            super.firstSectionToOpen()
        }
    }

    private fun isEnrollmentDataCompleted(): Boolean {
        val program = conf.program()
        val enrollment = conf.enrollment()

        val hasEnrollmentDate = enrollment?.enrollmentDate() != null
        if (!hasEnrollmentDate) return false

        if (program?.displayIncidentDate() == true) {
            val hasIncidentDate = enrollment?.incidentDate() != null
            if (!hasIncidentDate) return false
        }

        val hasOrganisationUnit = enrollment?.organisationUnit() != null
        if (!hasOrganisationUnit) return false

        if (conf.trackedEntityType()?.featureType() != FeatureType.NONE) {
            val hasTeiCoordinates = conf.tei()?.geometry() != null
            if (!hasTeiCoordinates) return false
        }

        if (program?.featureType() != FeatureType.NONE) {
            val hasEnrollmentCoordinates = enrollment?.geometry() != null
            if (!hasEnrollmentCoordinates) return false
        }

        return true
    }

    companion object {

        const val ENROLLMENT_DATA_SECTION_UID = "ENROLLMENT_DATA_SECTION_UID"
        const val ENROLLMENT_DATE_UID = "ENROLLMENT_DATE_UID"
        const val INCIDENT_DATE_UID = "INCIDENT_DATE_UID"
        const val ORG_UNIT_UID = "ORG_UNIT_UID"
        const val TEI_COORDINATES_UID = "TEI_COORDINATES_UID"
        const val ENROLLMENT_COORDINATES_UID = "ENROLLMENT_COORDINATES_UID"
    }
}
