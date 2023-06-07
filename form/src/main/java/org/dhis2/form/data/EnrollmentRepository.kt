package org.dhis2.form.data

import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.commons.date.DateUtils
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.SectionUiModelImpl.Companion.SINGLE_SECTION_UID
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramSection
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository
import timber.log.Timber

class EnrollmentRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val enrollmentUid: String,
    private val d2: D2,
    private val enrollmentMode: EnrollmentMode,
    private val enrollmentFormLabelsProvider: EnrollmentFormLabelsProvider
) : DataEntryBaseRepository(d2, fieldFactory) {

    private val enrollmentRepository: EnrollmentObjectRepository =
        d2.enrollmentModule().enrollments().uid(enrollmentUid)

    private fun canBeEdited(): Boolean {
        val selectedProgram = d2.programModule().programs().uid(
            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().program()
        ).blockingGet()
        val programAccess =
            selectedProgram.access().data().write() != null && selectedProgram.access().data()
                .write()
        val teTypeAccess = d2.trackedEntityModule().trackedEntityTypes().uid(
            selectedProgram.trackedEntityType()?.uid()
        ).blockingGet().access().data().write()
        return programAccess && teTypeAccess
    }

    private val program by lazy {
        d2.programModule().programs().uid(enrollmentRepository.blockingGet().program()).get()
    }

    private val programSections by lazy {
        d2.programModule().programSections().withAttributes()
            .byProgramUid().eq(enrollmentRepository.blockingGet().program())
            .blockingGet()
    }

    override fun sectionUids(): Flowable<MutableList<String>> {
        val sectionUids = mutableListOf(ENROLLMENT_DATA_SECTION_UID)
        sectionUids.addAll(programSections.map { it.uid() })
        return Flowable.just(sectionUids)
    }

    override fun list(): Flowable<MutableList<FieldUiModel>> {
        return program
            .flatMap { program ->
                d2.programModule().programSections().byProgramUid().eq(program.uid())
                    .withAttributes().get()
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
                        val fields = getEnrollmentData(program)
                        fields.addAll(list)
                        fields.add(fieldFactory.createClosingSection())
                        fields
                    }
            }.toFlowable()
    }

    override fun isEvent(): Boolean {
        return false
    }

    private fun getSingleSectionList(): MutableList<FieldUiModel> {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(enrollmentRepository.blockingGet().trackedEntityInstance())
            .blockingGet()
        val teiType = d2.trackedEntityModule().trackedEntityTypes()
            .uid(tei.trackedEntityType()).blockingGet()
        return mutableListOf(
            fieldFactory.createSingleSection(
                String.format(
                    enrollmentFormLabelsProvider.provideSingleSectionLabel(),
                    teiType.displayName()
                )
            )
        )
    }

    private fun getFieldsForSingleSection(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val programAttributes =
                d2.programModule().programTrackedEntityAttributes().withRenderType()
                    .byProgram().eq(program.blockingGet().uid())
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()

            programAttributes.map { programTrackedEntityAttribute ->
                transform(programTrackedEntityAttribute)
            }
        }
    }

    private fun getFieldsForMultipleSections(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val fields = mutableListOf<FieldUiModel>()
            programSections.forEach { section ->
                fields.add(
                    transformSection(section.uid(), section.displayName(), section.description())
                )
                section.attributes()?.forEachIndexed { _, attribute ->
                    d2.programModule().programTrackedEntityAttributes().withRenderType()
                        .byProgram().eq(program.blockingGet().uid())
                        .byTrackedEntityAttribute().eq(attribute.uid())
                        .one().blockingGet()?.let { programTrackedEntityAttribute ->
                            fields.add(transform(programTrackedEntityAttribute, section.uid()))
                        }
                }
            }
            return@fromCallable fields
        }
    }

    private fun transform(
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute,
        sectionUid: String? = SINGLE_SECTION_UID
    ): FieldUiModel {
        val attribute = d2.trackedEntityModule().trackedEntityAttributes()
            .uid(programTrackedEntityAttribute.trackedEntityAttribute()!!.uid())
            .blockingGet()
        val attrValueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(
                attribute!!.uid(),
                enrollmentRepository.blockingGet()!!.trackedEntityInstance()
            )

        val valueType = attribute.valueType()
        var mandatory = programTrackedEntityAttribute.mandatory()!!
        val optionSet = attribute.optionSet()?.uid()
        val generated = attribute.generated()!!

        val orgUnitUid = enrollmentRepository.blockingGet()!!.organisationUnit()

        var dataValue: String? = getAttributeValue(attrValueRepository)

        var optionSetConfig: OptionSetConfiguration? = null
        if (!optionSet.isNullOrEmpty()) {
            val optionCount =
                d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount()
            optionSetConfig = OptionSetConfiguration.config(
                optionCount
            ) {
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()
            }
        }

        var (error, warning) = getConflictErrorsAndWarnings(attribute.uid(), dataValue)

        if (generated && dataValue == null) {
            mandatory = true
            val result = handleAutogeneratedValue(attribute, orgUnitUid!!)
            dataValue = result.first
            warning = result.second
            if (!dataValue.isNullOrEmpty()) {
                attrValueRepository.blockingSet(dataValue)
            }
        }

        if ((valueType == ValueType.ORGANISATION_UNIT || valueType?.isDate == true) &&
            !dataValue.isNullOrEmpty()
        ) {
            dataValue = attrValueRepository.blockingGet().value()
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
            if (valueType == ValueType.COORDINATE) FeatureType.POINT else null
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
        dataValue: String?
    ): Pair<String?, String?> {
        var error: String? = null
        var warning: String? = null

        val conflicts = d2.importModule().trackerImportConflicts()
            .byEnrollmentUid().eq(enrollmentUid)
            .blockingGet()

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

    private fun getAttributeValue(
        attrValueRepository: TrackedEntityAttributeValueObjectRepository
    ) = if (attrValueRepository.blockingExists()) {
        attrValueRepository.blockingGet().userFriendlyValue(d2)
    } else {
        null
    }

    private fun handleAutogeneratedValue(
        attr: TrackedEntityAttribute,
        orgUnitUid: String
    ): Pair<String?, String?> {
        var warning: String? = null
        var dataValue: String? = null
        try {
            val teiUid = enrollmentRepository.blockingGet()!!.trackedEntityInstance()

            if (teiUid != null) {
                try {
                    dataValue = d2.trackedEntityModule().reservedValueManager()
                        .blockingGetValue(attr.uid(), orgUnitUid)
                } catch (e: Exception) {
                    dataValue = null
                    warning = enrollmentFormLabelsProvider.provideReservedValueWarning()
                }

                if (attr.valueType() == ValueType.NUMBER) {
                    while (dataValue!!.startsWith("0")) {
                        dataValue = d2.trackedEntityModule().reservedValueManager()
                            .blockingGetValue(attr.uid(), orgUnitUid)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            warning = enrollmentFormLabelsProvider.provideReservedValueWarning()
        }

        return Pair(dataValue, warning)
    }

    private fun getEnrollmentData(program: Program): MutableList<FieldUiModel> {
        val enrollmentDataList = ArrayList<FieldUiModel>()
        enrollmentDataList.add(getEnrollmentDataSection(program.description()))

        enrollmentDataList.add(
            getEnrollmentDateField(
                program.enrollmentDateLabel()
                    ?: enrollmentFormLabelsProvider.provideEnrollmentDateDefaultLabel(),
                program.selectEnrollmentDatesInFuture()
            )
        )
        if (program.displayIncidentDate()!!) {
            enrollmentDataList.add(
                getIncidentDateField(
                    program.incidentDateLabel()
                        ?: enrollmentFormLabelsProvider.provideIncidentDateDefaultLabel(),
                    program.selectIncidentDatesInFuture()
                )
            )
        }
        val orgUnits = d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byProgramUids(listOf(enrollmentRepository.blockingGet().program())).blockingCount()
        enrollmentDataList.add(
            getOrgUnitField(enrollmentMode == EnrollmentMode.NEW && orgUnits > 1)
        )

        val teiType =
            d2.trackedEntityModule().trackedEntityTypes()
                .uid(program.trackedEntityType()!!.uid())
                .blockingGet()
        if (teiType!!.featureType() != null && teiType.featureType() != FeatureType.NONE) {
            enrollmentDataList.add(getTeiCoordinatesField(teiType.featureType()))
        }

        if (program.featureType() != null && program.featureType() != FeatureType.NONE) {
            enrollmentDataList.add(
                getEnrollmentCoordinatesField(
                    program.featureType()
                )
            )
        }

        return enrollmentDataList
    }

    private fun getEnrollmentDataSection(description: String?): FieldUiModel {
        return fieldFactory.createSection(
            ENROLLMENT_DATA_SECTION_UID,
            enrollmentFormLabelsProvider.provideEnrollmentDataSectionLabel(),
            description,
            false,
            0,
            0,
            SectionRenderingType.LISTING.name
        )
    }

    private fun getEnrollmentDateField(
        enrollmentDateLabel: String,
        allowFutureDates: Boolean?
    ): FieldUiModel {
        return fieldFactory.create(
            ENROLLMENT_DATE_UID,
            enrollmentDateLabel,
            ValueType.DATE,
            true, // check in constructor of dateviewmodel
            null,
            when (val date = enrollmentRepository.blockingGet()!!.enrollmentDate()) {
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
            null
        )
    }

    private fun getIncidentDateField(
        incidentDateLabel: String,
        allowFutureDates: Boolean?
    ): FieldUiModel {
        return fieldFactory.create(
            INCIDENT_DATE_UID,
            incidentDateLabel,
            ValueType.DATE,
            true,
            null,
            when (val date = enrollmentRepository.blockingGet()!!.incidentDate()) {
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
            null
        )
    }

    private fun getOrgUnitField(editable: Boolean): FieldUiModel {
        return fieldFactory.create(
            ORG_UNIT_UID,
            enrollmentFormLabelsProvider.provideEnrollmentOrgUnitLabel(),
            ValueType.ORGANISATION_UNIT,
            true,
            null,
            enrollmentRepository.blockingGet()?.organisationUnit(),
            ENROLLMENT_DATA_SECTION_UID,
            null,
            editable,
            SectionRenderingType.LISTING,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            null
        )
    }

    private fun getTeiCoordinatesField(featureType: FeatureType?): FieldUiModel {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(
                enrollmentRepository.blockingGet()!!.trackedEntityInstance()
            ).blockingGet()
        val teiType = d2.trackedEntityModule().trackedEntityTypes()
            .uid(tei.trackedEntityType()).blockingGet()
        val teiCoordinatesLabel = enrollmentFormLabelsProvider.provideTeiCoordinatesLabel()
        return fieldFactory.create(
            TEI_COORDINATES_UID,
            "$teiCoordinatesLabel ${teiType.displayName()}",
            ValueType.COORDINATE,
            false,
            null,
            if (tei!!.geometry() != null) tei.geometry()!!.coordinates() else null,
            ENROLLMENT_DATA_SECTION_UID,
            null,
            canBeEdited(),
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            featureType
        )
    }

    private fun getEnrollmentCoordinatesField(featureType: FeatureType?): FieldUiModel {
        return fieldFactory.create(
            ENROLLMENT_COORDINATES_UID,
            enrollmentFormLabelsProvider.provideEnrollmentCoordinatesLabel(),
            ValueType.COORDINATE,
            false,
            null,
            if (enrollmentRepository.blockingGet()!!.geometry() != null) {
                enrollmentRepository.blockingGet()!!.geometry()!!.coordinates()
            } else {
                null
            },
            ENROLLMENT_DATA_SECTION_UID,
            null,
            canBeEdited(),
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            featureType
        )
    }

    fun hasEventsGeneratedByEnrollmentDate(): Boolean {
        val enrollment = enrollmentRepository.blockingGet()
        val stagesWithReportDateToUse = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byOpenAfterEnrollment().isTrue
            .byReportDateToUse().eq("enrollmentDate")
            .blockingGetUids()
        val stagesWithGeneratedBy = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byAutoGenerateEvent().isTrue
            .byGeneratedByEnrollmentDate().isTrue
            .blockingGetUids()
        return !d2.eventModule().events()
            .byTrackedEntityInstanceUids(arrayListOf(enrollment.trackedEntityInstance()))
            .byProgramStageUid().`in`(stagesWithReportDateToUse.union(stagesWithGeneratedBy))
            .blockingIsEmpty()
    }

    fun hasEventsGeneratedByIncidentDate(): Boolean {
        val enrollment = enrollmentRepository.blockingGet()
        val stagesWithReportDateToUse = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byOpenAfterEnrollment().isTrue
            .byReportDateToUse().eq("incidentDate")
            .blockingGetUids()
        val stagesWithGeneratedBy = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byAutoGenerateEvent().isTrue
            .byGeneratedByEnrollmentDate().isFalse
            .blockingGetUids()
        return !d2.eventModule().events()
            .byTrackedEntityInstanceUids(arrayListOf(enrollment.trackedEntityInstance()))
            .byProgramStageUid().`in`(stagesWithReportDateToUse.union(stagesWithGeneratedBy))
            .blockingIsEmpty()
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
