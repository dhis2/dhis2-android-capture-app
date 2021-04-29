package org.dhis2.form.data

import org.dhis2.form.model.EnrollmentDetail
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.utils.FormatUtilsProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository

class FormEnrollmentRepository(
    private val repositoryImpl: FormRepositoryImpl,
    private val d2: D2,
    private val enrollmentUid: String,
    private val formatUtilsProvider: FormatUtilsProvider
) : FormRepository by repositoryImpl {

    init {
        repositoryImpl.storeValue = { uid: String, value: String?, extraData: String? ->
            store(uid, value, extraData)
        }
    }

    private fun store(uid: String, value: String?, extraData: String?): StoreResult {
        val enrollmentRepository = d2.enrollmentModule().enrollments().uid(enrollmentUid)
        return when (uid) {
            EnrollmentDetail.ENROLLMENT_DATE_UID.name -> {
                enrollmentRepository.setEnrollmentDate(
                    value?.let {
                        formatUtilsProvider.stringToDate(it)
                    }
                )

                StoreResult(
                    EnrollmentDetail.ENROLLMENT_DATE_UID.name,
                    ValueStoreResult.VALUE_CHANGED
                )

            }
            EnrollmentDetail.INCIDENT_DATE_UID.name -> {
                enrollmentRepository.setIncidentDate(
                    value?.let {
                        formatUtilsProvider.stringToDate(it)
                    }
                )

                StoreResult(EnrollmentDetail.INCIDENT_DATE_UID.name, ValueStoreResult.VALUE_CHANGED)
            }
            EnrollmentDetail.ORG_UNIT_UID.name -> {
                StoreResult("", ValueStoreResult.VALUE_CHANGED)
            }
            EnrollmentDetail.TEI_COORDINATES_UID.name -> {
                val geometry = value?.let {
                    extraData?.let {
                        Geometry.builder()
                            .coordinates(value)
                            .type(FeatureType.valueOf(it))
                            .build()
                    }
                }
                saveTeiGeometry(geometry)
                StoreResult("", ValueStoreResult.VALUE_CHANGED)

            }
            EnrollmentDetail.ENROLLMENT_COORDINATES_UID.name -> {
                val geometry = value?.let {
                    extraData?.let {
                        Geometry.builder()
                            .coordinates(value)
                            .type(FeatureType.valueOf(it))
                            .build()
                    }
                }
                saveEnrollmentGeometry(geometry)
                StoreResult("", ValueStoreResult.VALUE_CHANGED)
            }
            else -> saveAttribute(uid, value)
        }
    }

    private fun saveTeiGeometry(geometry: Geometry?) {
        val enrollmentRepository = d2.enrollmentModule().enrollments().uid(enrollmentUid)
        val teiRepository = d2.trackedEntityModule().trackedEntityInstances()
            .uid(enrollmentRepository.blockingGet().trackedEntityInstance())
        teiRepository.setGeometry(geometry)
    }

    private fun saveEnrollmentGeometry(geometry: Geometry?) {
        val enrollmentRepository = d2.enrollmentModule().enrollments().uid(enrollmentUid)
        enrollmentRepository.setGeometry(geometry)
    }

    private fun saveAttribute(uid: String, value: String?): StoreResult {
        val teiUid = d2.enrollmentModule()
            .enrollments()
            .uid(enrollmentUid)
            .blockingGet()
            .trackedEntityInstance()!!

        if (!checkUniqueFilter(uid, value, teiUid)) {
            return StoreResult(uid, ValueStoreResult.VALUE_NOT_UNIQUE)
        }

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(uid, teiUid)
        val valueType =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet().valueType()
        var newValue = repositoryImpl.withValueTypeCheck(value, valueType) ?: ""
        if (valueType == ValueType.IMAGE && value != null) {
            newValue = repositoryImpl.saveFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            repositoryImpl.withValueTypeCheck(valueRepository.blockingGet().value(), valueType)
        } else {
            ""
        }
        return if (currentValue != newValue) {
            if (!value.isNullOrEmpty()) {
                blockingSetCheck(valueRepository, uid, newValue)
            } else {
                valueRepository.blockingDeleteIfExist()
            }
            StoreResult(uid, ValueStoreResult.VALUE_CHANGED)
        } else {
            StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun blockingSetCheck(
        valueRepository: TrackedEntityAttributeValueObjectRepository,
        attrUid: String,
        value: String
    ): Boolean {
        return d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet().let {
            if (check(d2, it.valueType(), it.optionSet()?.uid(), value)) {
                val finalValue = repositoryImpl.assureCodeForOptionSet(it.optionSet()?.uid(), value)
                valueRepository.blockingSet(finalValue)
                true
            } else {
                valueRepository.blockingDeleteIfExist()
                false
            }
        }

    }

    private fun check(
        d2: D2,
        valueType: ValueType?,
        optionSetUid: String?,
        value: String
    ): Boolean {
        return when {
            optionSetUid != null -> {
                val optionByCodeExist =
                    d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
                        .byCode().eq(value).one().blockingExists()
                val optionByNameExist =
                    d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
                        .byDisplayName().eq(value).one().blockingExists()
                optionByCodeExist || optionByNameExist
            }
            valueType != null -> {
                if (valueType.isNumeric) {
                    try {
                        value.toFloat().toString()
                        true
                    } catch (e: Exception) {
                        false
                    }
                } else {
                    when (valueType) {
                        ValueType.FILE_RESOURCE, ValueType.IMAGE ->
                            d2.fileResourceModule().fileResources()
                                .byUid().eq(value).one().blockingExists()
                        ValueType.ORGANISATION_UNIT ->
                            d2.organisationUnitModule().organisationUnits().uid(value)
                                .blockingExists()
                        else -> true
                    }
                }
            }
            else -> false
        }
    }

    private fun checkUniqueFilter(uid: String, value: String?, teiUid: String): Boolean {
        if (value == null) {
            return true
        }

        val localUid =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = localUid.unique() ?: false
        val orgUnitScope = localUid.orgUnitScope() ?: false

        if (!isUnique) {
            return true
        }

        return if (!orgUnitScope) {
            val hasValue = getTrackedEntityAttributeValues(uid, value, teiUid).isNotEmpty()
            !hasValue
        } else {
            val enrollingOrgUnit = getOrgUnit(teiUid)
            val hasValue = getTrackedEntityAttributeValues(uid, value, teiUid)
                .map {
                    getOrgUnit(it.trackedEntityInstance()!!)
                }
                .all { it != enrollingOrgUnit }
            hasValue
        }
    }

    private fun getTrackedEntityAttributeValues(
        uid: String,
        value: String,
        teiUid: String
    ): List<TrackedEntityAttributeValue> {
        return d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityAttribute().eq(uid)
            .byTrackedEntityInstance().neq(teiUid)
            .byValue().eq(value).blockingGet()
    }

    private fun getOrgUnit(teiUid: String): String? {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
            .organisationUnit()
    }
}
