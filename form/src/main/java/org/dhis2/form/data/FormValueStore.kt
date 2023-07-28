package org.dhis2.form.data

import io.reactivex.Flowable
import java.io.File
import org.dhis2.Bindings.blockingSetCheck
import org.dhis2.Bindings.withValueTypeCheck
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.extensions.toDate
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.EnrollmentDetail
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

class FormValueStore(
    private val d2: D2,
    private val recordUid: String,
    private val entryMode: EntryMode,
    private val enrollmentRepository: EnrollmentObjectRepository?,
    private val crashReportController: CrashReportController,
    private val networkUtils: NetworkUtils,
    private val resourceManager: ResourceManager
) {

    fun save(uid: String, value: String?, extraData: String?): StoreResult {
        return when (entryMode) {
            EntryMode.DE ->
                saveDataElement(uid, value).blockingSingle()
            EntryMode.ATTR ->
                checkStoreEnrollmentDetail(uid, value, extraData).blockingSingle()
            EntryMode.DV ->
                throw IllegalArgumentException(
                    resourceManager.getString(R.string.data_values_save_error)
                )
        }
    }

    private fun checkStoreEnrollmentDetail(
        uid: String,
        value: String?,
        extraData: String?
    ): Flowable<StoreResult> {
        return when (uid) {
            EnrollmentDetail.ENROLLMENT_DATE_UID.name -> {
                enrollmentRepository?.setEnrollmentDate(
                    value?.toDate()
                )

                Flowable.just(
                    StoreResult(
                        EnrollmentDetail.ENROLLMENT_DATE_UID.name,
                        ValueStoreResult.VALUE_CHANGED
                    )
                )
            }
            EnrollmentDetail.INCIDENT_DATE_UID.name -> {
                enrollmentRepository?.setIncidentDate(
                    value?.toDate()
                )

                Flowable.just(
                    StoreResult(
                        EnrollmentDetail.INCIDENT_DATE_UID.name,
                        ValueStoreResult.VALUE_CHANGED
                    )
                )
            }
            EnrollmentDetail.ORG_UNIT_UID.name -> {
                Flowable.just(
                    StoreResult(
                        "",
                        ValueStoreResult.VALUE_CHANGED
                    )
                )
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
                Flowable.just(
                    StoreResult(
                        "",
                        ValueStoreResult.VALUE_CHANGED
                    )
                )
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
                try {
                    saveEnrollmentGeometry(geometry)
                    return Flowable.just(
                        StoreResult(
                            "",
                            ValueStoreResult.VALUE_CHANGED
                        )
                    )
                } catch (d2Error: D2Error) {
                    val errorMessage = d2Error.errorDescription() + ": $geometry"
                    crashReportController.trackError(d2Error, errorMessage)
                    Flowable.just(
                        StoreResult(
                            "",
                            ValueStoreResult.ERROR_UPDATING_VALUE
                        )
                    )
                }
            }
            else -> saveAttribute(uid, value)
        }
    }

    private fun saveTeiGeometry(geometry: Geometry?) {
        val teiRepository = d2.trackedEntityModule().trackedEntityInstances()
            .uid(enrollmentRepository?.blockingGet()?.trackedEntityInstance())
        teiRepository.setGeometry(geometry)
    }

    private fun saveEnrollmentGeometry(geometry: Geometry?) {
        enrollmentRepository?.setGeometry(geometry)
    }

    private fun saveAttribute(uid: String, value: String?): Flowable<StoreResult> {
        val teiUid =
            when (entryMode) {
                EntryMode.DE -> {
                    val event = d2.eventModule().events().uid(recordUid).blockingGet()
                    val enrollment = d2.enrollmentModule().enrollments()
                        .uid(event.enrollment()).blockingGet()
                    enrollment.trackedEntityInstance()
                }
                EntryMode.ATTR -> recordUid
                EntryMode.DV -> null
            }
                ?: return Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED))

        if (!checkUniqueFilter(uid, value, teiUid)) {
            return Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_NOT_UNIQUE))
        }

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(uid, teiUid)
        val attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()
        val valueType = attribute.valueType()
        val optionSet = attribute.optionSet()
        var newValue = value.withValueTypeCheck(valueType) ?: ""
        if (optionSet == null && isFile(valueType) && value != null) {
            try {
                newValue = saveFileResource(value, valueType == ValueType.IMAGE)
            } catch (e: Exception) {
                return Flowable.just(
                    StoreResult(
                        uid = uid,
                        valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
                        valueStoreResultMessage = e.localizedMessage
                    )
                )
            }
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value().withValueTypeCheck(valueType)
        } else {
            ""
        }
        return if (currentValue != newValue) {
            if (!value.isNullOrEmpty()) {
                valueRepository.blockingSetCheck(d2, uid, newValue) { _attrUid, _value ->
                    crashReportController.addBreadCrumb(
                        "blockingSetCheck Crash",
                        "Attribute: $_attrUid," +
                            "" + " value: $_value"
                    )
                }
            } else {
                valueRepository.blockingDeleteIfExist()
            }
            Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_CHANGED))
        } else {
            Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED))
        }
    }

    private fun checkUniqueFilter(uid: String, value: String?, teiUid: String): Boolean {
        return if (!networkUtils.isOnline()) {
            isTrackedEntityAttributeValueUnique(uid, value, teiUid)
        } else {
            val programUid = enrollmentRepository?.blockingGet()?.program()
            isUniqueTEIAttributeOnline(uid, value, teiUid, programUid)
        }
    }

    private fun isUniqueTEIAttributeOnline(
        uid: String,
        value: String?,
        teiUid: String,
        programUid: String?
    ): Boolean {
        if (value == null || programUid == null) {
            return true
        }

        val attribute =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = attribute.unique() ?: false
        val orgUnitScope = attribute.orgUnitScope() ?: false

        if (isUnique && !orgUnitScope) {
            try {
                val teiList = d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                    .allowOnlineCache()
                    .eq(true)
                    .byOrgUnitMode()
                    .eq(OrganisationUnitMode.ACCESSIBLE)
                    .byProgram()
                    .eq(programUid)
                    .byAttribute(attribute.uid()).eq(value).blockingGet()

                if (teiList.isNullOrEmpty()) {
                    return true
                }

                return teiList.none { it.uid() != teiUid }
            } catch (e: Exception) {
                return trackSentryError(e, programUid, attribute, value)
            }
        } else if (isUnique && orgUnitScope) {
            val orgUnit = getOrgUnit(teiUid)

            val teiList = d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache()
                .eq(true)
                .byProgram()
                .eq(programUid)
                .byAttribute(attribute.uid())
                .eq(value)
                .byOrgUnitMode()
                .eq(OrganisationUnitMode.DESCENDANTS)
                .byOrgUnits()
                .`in`(orgUnit)
                .blockingGet()

            if (teiList.isNullOrEmpty()) {
                return true
            }

            return teiList.none { it.uid() != teiUid }
        }
        return true
    }

    private fun trackSentryError(
        e: Exception,
        programUid: String?,
        attribute: TrackedEntityAttribute,
        value: String?
    ): Boolean {
        val exception = if (e.cause != null && e.cause is D2Error) {
            val d2Error = e.cause as D2Error
            "component: ${d2Error.errorComponent()}," +
                " code: ${d2Error.errorCode()}," +
                " description: ${d2Error.errorDescription()}"
        } else {
            "No d2 Error"
        }
        crashReportController.addBreadCrumb(
            "SearchTEIRepositoryImpl.isUniqueAttribute",
            "programUid: $programUid ," +
                " attruid: ${attribute.uid()} ," +
                " attrvalue: $value, $exception"
        )
        return true
    }

    private fun isTrackedEntityAttributeValueUnique(
        uid: String,
        value: String?,
        teiUid: String
    ): Boolean {
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

    fun getOrgUnit(teiUid: String): String? {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
            .organisationUnit()
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

    private fun saveFileResource(path: String, resize: Boolean): String {
        val file = if (resize) {
            FileResizerHelper.resizeFile(File(path), FileResizerHelper.Dimension.MEDIUM)
        } else {
            File(path)
        }
        return d2.fileResourceModule().fileResources().blockingAdd(file)
    }

    private fun saveDataElement(uid: String, value: String?): Flowable<StoreResult> {
        val valueRepository = d2.trackedEntityModule().trackedEntityDataValues()
            .value(recordUid, uid)
        val dataElement = d2.dataElementModule().dataElements().uid(uid).blockingGet()
        val valueType = dataElement.valueType()
        val optionSet = dataElement.optionSet()
        var newValue = value.withValueTypeCheck(valueType) ?: ""
        if (optionSet == null && isFile(valueType) && value != null) {
            try {
                newValue = saveFileResource(value, valueType == ValueType.IMAGE)
            } catch (e: Exception) {
                return Flowable.just(
                    StoreResult(
                        uid = uid,
                        valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
                        valueStoreResultMessage = e.localizedMessage
                    )
                )
            }
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value().withValueTypeCheck(valueType)
        } else {
            ""
        }

        return if (currentValue != newValue) {
            if (!value.isNullOrEmpty()) {
                if (valueRepository.blockingSetCheck(d2, uid, newValue)) {
                    Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_CHANGED))
                } else {
                    Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED))
                }
            } else {
                valueRepository.blockingDeleteIfExist()
                Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_CHANGED))
            }
        } else {
            Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED))
        }
    }

    fun saveWithTypeCheck(uid: String, value: String?): Flowable<StoreResult> {
        return when {
            d2.dataElementModule().dataElements().uid(uid).blockingExists() ->
                saveDataElement(uid, value)
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingExists() ->
                saveAttribute(uid, value)
            else -> Flowable.just(StoreResult(uid, ValueStoreResult.UID_IS_NOT_DE_OR_ATTR))
        }
    }

    fun deleteOptionValueIfSelected(field: String, optionUid: String): StoreResult {
        return when (entryMode) {
            EntryMode.DE -> deleteDataElementValue(field, optionUid)
            EntryMode.ATTR -> deleteAttributeValue(field, optionUid)
            EntryMode.DV
            -> throw IllegalArgumentException(
                resourceManager.getString(R.string.data_values_save_error)
            )
        }
    }

    private fun deleteDataElementValue(field: String, optionUid: String): StoreResult {
        val option = d2.optionModule().options().uid(optionUid).blockingGet()
        val possibleValues = arrayListOf(option.name()!!, option.code()!!)
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(recordUid, field)
        return if (valueRepository.blockingExists() &&
            possibleValues.contains(valueRepository.blockingGet().value())
        ) {
            saveDataElement(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun deleteAttributeValue(field: String, optionUid: String): StoreResult {
        val option = d2.optionModule().options().uid(optionUid).blockingGet()
        val possibleValues = arrayListOf(option.name()!!, option.code()!!)
        val valueRepository =
            d2.trackedEntityModule().trackedEntityAttributeValues().value(field, recordUid)
        return if (valueRepository.blockingExists() &&
            possibleValues.contains(valueRepository.blockingGet().value())
        ) {
            saveAttribute(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    fun deleteOptionValueIfSelectedInGroup(
        field: String,
        optionGroupUid: String,
        isInGroup: Boolean
    ): StoreResult {
        val optionsInGroup =
            d2.optionModule().optionGroups()
                .withOptions()
                .uid(optionGroupUid)
                .blockingGet()
                .options()
                ?.map { d2.optionModule().options().uid(it.uid()).blockingGet().code()!! }
                ?: arrayListOf()
        return when (entryMode) {
            EntryMode.DE -> deleteDataElementValueIfNotInGroup(
                field,
                optionsInGroup,
                isInGroup
            )
            EntryMode.ATTR -> deleteAttributeValueIfNotInGroup(
                field,
                optionsInGroup,
                isInGroup
            )
            EntryMode.DV
            -> throw IllegalArgumentException(
                "DataValues can't be saved using these arguments. Use the other one."
            )
        }
    }

    private fun deleteAttributeValueIfNotInGroup(
        field: String,
        optionCodesToShow: List<String>,
        isInGroup: Boolean
    ): StoreResult {
        val valueRepository =
            d2.trackedEntityModule().trackedEntityAttributeValues().value(field, recordUid)
        return if (valueRepository.blockingExists() &&
            optionCodesToShow.contains(valueRepository.blockingGet().value()) == isInGroup
        ) {
            saveAttribute(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun deleteDataElementValueIfNotInGroup(
        field: String,
        optionCodesToShow: List<String>,
        isInGroup: Boolean
    ): StoreResult {
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(recordUid, field)
        return if (valueRepository.blockingExists() &&
            optionCodesToShow.contains(valueRepository.blockingGet().value()) == isInGroup
        ) {
            saveDataElement(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun isFile(valueType: ValueType?): Boolean {
        return valueType == ValueType.IMAGE || valueType?.isFile == true
    }
}
