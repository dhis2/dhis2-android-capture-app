package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import io.reactivex.Single
import java.util.ArrayList
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.form.data.DataEntryBaseRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.SectionUiModelImpl.Companion.SINGLE_SECTION_UID
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.utils.DateUtils
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramSection
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import timber.log.Timber

class EnrollmentRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val enrollmentUid: String,
    private val d2: D2,
    private val dhisEnrollmentUtils: DhisEnrollmentUtils,
    private val enrollmentMode: EnrollmentActivity.EnrollmentMode,
    private val enrollmentDataSectionLabel: String,
    private val singleSectionLabel: String,
    private val enrollmentOrgUnitLabel: String,
    private val teiCoordinatesLabel: String,
    private val enrollmentCoordinatesLabel: String,
    private val reservedValuesWarning: String,
    private val enrollmentDateDefaultLabel: String,
    private val incidentDateDefaultLabel: String
) : DataEntryBaseRepository(d2, fieldFactory) {

    private val enrollmentRepository: EnrollmentObjectRepository =
        d2.enrollmentModule().enrollments().uid(enrollmentUid)

    private val canEditAttributes: Boolean = dhisEnrollmentUtils.canBeEdited(enrollmentUid)

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
                    singleSectionLabel,
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
        val optionSet =
            if (attribute.optionSet() != null) attribute.optionSet()!!.uid() else null
        val generated = attribute.generated()!!

        val orgUnitUid = enrollmentRepository.blockingGet()!!.organisationUnit()

        var dataValue: String? = if (attrValueRepository.blockingExists()) {
            attrValueRepository.blockingGet().userFriendlyValue(d2)
        } else {
            null
        }

        var optionCount = 0
        var options = listOf<Option>()
        if (!DhisTextUtils.isEmpty(optionSet)) {
            optionCount =
                d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount()
            options =
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()
        }
        var warning: String? = null

        if (generated && dataValue == null) {
            mandatory = true
            val result = handleAutogeneratedValue(attribute, orgUnitUid!!)
            dataValue = result.first
            warning = result.second
            if (!DhisTextUtils.isEmpty(dataValue)) {
                attrValueRepository.blockingSet(dataValue)
            }
        }

        val conflicts = d2.importModule().trackerImportConflicts()
            .byEnrollmentUid().eq(enrollmentUid)
            .blockingGet()

        val conflict = conflicts
            .find { it.trackedEntityAttribute() == attribute.uid() }

        val error = conflict?.let {
            if (it.value() == dataValue) {
                it.displayDescription()
            } else {
                null
            }
        }

        if ((valueType == ValueType.ORGANISATION_UNIT || valueType?.isDate == true) &&
            !DhisTextUtils.isEmpty(dataValue)
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

        val renderingType = if (programSection?.renderType() != null &&
            programSection.renderType()!!.mobile() != null
        ) {
            programSection.renderType()!!.mobile()!!.type()
        } else {
            null
        }

        val fieldViewModel = fieldFactory.create(
            attribute.uid(),
            attribute.displayFormName() ?: "",
            valueType!!,
            mandatory,
            optionSet,
            dataValue,
            sectionUid,
            programTrackedEntityAttribute.allowFutureDate() ?: false,
            !generated && canEditAttributes,
            renderingType,
            attribute.displayDescription(),
            programTrackedEntityAttribute.renderType()?.mobile(),
            optionCount,
            attribute.style(),
            attribute.fieldMask(),
            options,
            if (valueType == ValueType.COORDINATE) FeatureType.POINT else null
        )

        return if (!error.isNullOrEmpty()) {
            fieldViewModel.setError(error)
        } else if (warning != null) {
            fieldViewModel.setWarning(warning)
        } else {
            fieldViewModel
        }
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
                    warning = reservedValuesWarning
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
            warning = reservedValuesWarning
        }

        return Pair(dataValue, warning)
    }

    private fun getEnrollmentData(program: Program): MutableList<FieldUiModel> {
        val enrollmentDataList = ArrayList<FieldUiModel>()
        enrollmentDataList.add(getEnrollmentDataSection(program.description()))

        enrollmentDataList.add(
            getEnrollmentDateField(
                program.enrollmentDateLabel() ?: enrollmentDateDefaultLabel,
                program.selectEnrollmentDatesInFuture()
            )
        )
        if (program.displayIncidentDate()!!) {
            enrollmentDataList.add(
                getIncidentDateField(
                    program.incidentDateLabel() ?: incidentDateDefaultLabel,
                    program.selectIncidentDatesInFuture()
                )
            )
        }
        val orgUnits = d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byProgramUids(listOf(enrollmentRepository.blockingGet().program())).blockingCount()
        enrollmentDataList.add(
            getOrgUnitField(enrollmentMode == EnrollmentActivity.EnrollmentMode.NEW && orgUnits > 1)
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
            enrollmentDataSectionLabel,
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
            canEditAttributes,
            null,
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
            canEditAttributes,
            null,
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
            enrollmentOrgUnitLabel,
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
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            null
        )
    }

    private fun getTeiCoordinatesField(
        featureType: FeatureType?
    ): FieldUiModel {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(
                enrollmentRepository.blockingGet()!!.trackedEntityInstance()
            ).blockingGet()
        val teiType = d2.trackedEntityModule().trackedEntityTypes()
            .uid(tei.trackedEntityType()).blockingGet()
        return fieldFactory.create(
            TEI_COORDINATES_UID,
            "$teiCoordinatesLabel ${teiType.displayName()}",
            ValueType.COORDINATE,
            false,
            null,
            if (tei!!.geometry() != null) tei.geometry()!!.coordinates() else null,
            ENROLLMENT_DATA_SECTION_UID,
            null,
            canEditAttributes,
            null,
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            null,
            null,
            featureType
        )
    }

    private fun getEnrollmentCoordinatesField(
        featureType: FeatureType?
    ): FieldUiModel {
        return fieldFactory.create(
            ENROLLMENT_COORDINATES_UID,
            enrollmentCoordinatesLabel,
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
            canEditAttributes,
            null,
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
        return dhisEnrollmentUtils.hasEventsGeneratedByEnrollmentDate(
            enrollmentRepository.blockingGet()
        )
    }

    fun hasEventsGeneratedByIncidentDate(): Boolean {
        return dhisEnrollmentUtils.hasEventsGeneratedByIncidentDate(
            enrollmentRepository.blockingGet()
        )
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
