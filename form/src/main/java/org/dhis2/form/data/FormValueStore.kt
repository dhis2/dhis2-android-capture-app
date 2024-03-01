package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.bindings.blockingSetCheck
import org.dhis2.bindings.withValueTypeCheck
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
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.maintenance.D2Error
import java.io.File

class FormValueStore(
    private val d2: D2,
    private val recordUid: String,
    private val entryMode: EntryMode,
    private val enrollmentRepository: EnrollmentObjectRepository?,
    private val crashReportController: CrashReportController,
    private val networkUtils: NetworkUtils,
    private val resourceManager: ResourceManager,
    private val fileController: FileController = FileController(),
    private val uniqueAttributeController: UniqueAttributeController = UniqueAttributeController(
        d2,
        crashReportController,
    ),
) {

    fun save(uid: String, value: String?, extraData: String?): StoreResult {
        return when (entryMode) {
            EntryMode.DE ->
                saveDataElement(uid, value).blockingSingle()

            EntryMode.ATTR ->
                checkStoreEnrollmentDetail(uid, value, extraData).blockingSingle()

            EntryMode.DV ->
                throw IllegalArgumentException(
                    resourceManager.getString(R.string.data_values_save_error),
                )
        }
    }

    fun storeFile(uid: String, filePath: String?): StoreResult {
        val valueType = when (entryMode) {
            EntryMode.DE ->
                d2.dataElementModule().dataElements()
                    .uid(uid)
                    .blockingGet()
                    ?.valueType()

            EntryMode.ATTR ->
                d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(uid)
                    .blockingGet()
                    ?.valueType()

            EntryMode.DV ->
                throw IllegalArgumentException(
                    resourceManager.getString(R.string.data_values_save_error),
                )
        }
        return filePath?.let {
            try {
                saveFileResource(filePath, valueType == ValueType.IMAGE)
            } catch (e: Exception) {
                return StoreResult(
                    uid = uid,
                    valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
                    valueStoreResultMessage = e.localizedMessage,
                )
            }
        }?.let { fileResourceUid ->
            StoreResult(
                uid = fileResourceUid,
                valueStoreResult = ValueStoreResult.FILE_SAVED,
            )
        } ?: StoreResult(
            uid = uid,
            valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
        )
    }

    private fun checkStoreEnrollmentDetail(
        uid: String,
        value: String?,
        extraData: String?,
    ): Flowable<StoreResult> {
        return when (uid) {
            EnrollmentDetail.ENROLLMENT_DATE_UID.name -> {
                enrollmentRepository?.setEnrollmentDate(
                    value?.toDate(),
                )

                Flowable.just(
                    StoreResult(
                        EnrollmentDetail.ENROLLMENT_DATE_UID.name,
                        ValueStoreResult.VALUE_CHANGED,
                    ),
                )
            }

            EnrollmentDetail.INCIDENT_DATE_UID.name -> {
                enrollmentRepository?.setIncidentDate(
                    value?.toDate(),
                )

                Flowable.just(
                    StoreResult(
                        EnrollmentDetail.INCIDENT_DATE_UID.name,
                        ValueStoreResult.VALUE_CHANGED,
                    ),
                )
            }

            EnrollmentDetail.ORG_UNIT_UID.name -> {
                try {
                    enrollmentRepository?.setOrganisationUnitUid(value)
                    Flowable.just(
                        StoreResult(
                            EnrollmentDetail.ORG_UNIT_UID.name,
                            ValueStoreResult.VALUE_CHANGED,
                        ),
                    )
                } catch (e: Exception) {
                    Flowable.just(
                        StoreResult(
                            EnrollmentDetail.ORG_UNIT_UID.name,
                            ValueStoreResult.ERROR_UPDATING_VALUE,
                        ),
                    )
                }
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
                        ValueStoreResult.VALUE_CHANGED,
                    ),
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
                            ValueStoreResult.VALUE_CHANGED,
                        ),
                    )
                } catch (d2Error: D2Error) {
                    val errorMessage = d2Error.errorDescription() + ": $geometry"
                    crashReportController.trackError(d2Error, errorMessage)
                    Flowable.just(
                        StoreResult(
                            "",
                            ValueStoreResult.ERROR_UPDATING_VALUE,
                        ),
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
                        .uid(event?.enrollment()).blockingGet()
                    enrollment?.trackedEntityInstance()
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
        val valueType = attribute?.valueType()
        val newValue = value.withValueTypeCheck(valueType) ?: ""

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet()?.value().withValueTypeCheck(valueType)
        } else {
            ""
        }
        return if (currentValue != newValue) {
            if (!value.isNullOrEmpty()) {
                valueRepository.blockingSetCheck(d2, uid, newValue) { _attrUid, _value ->
                    crashReportController.addBreadCrumb(
                        "blockingSetCheck Crash",
                        "Attribute: $_attrUid," +
                            "" + " value: $_value",
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
        programUid: String?,
    ): Boolean {
        if (value == null || programUid == null) {
            return true
        }

        val attribute =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = attribute.unique() ?: false
        val orgUnitScope = attribute.orgUnitScope() ?: false

        if (isUnique) {
            return uniqueAttributeController.checkAttributeOnline(
                orgUnitScope,
                programUid,
                teiUid,
                attribute.uid(),
                value,
            )
        }

        return true
    }

    private fun isTrackedEntityAttributeValueUnique(
        uid: String,
        value: String?,
        teiUid: String,
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

        return uniqueAttributeController.checkAttributeLocal(orgUnitScope, teiUid, uid, value)
    }

    private fun saveFileResource(path: String, resize: Boolean): String {
        val file = if (resize) {
            fileController.resize(path)
        } else {
            File(path)
        }
        return d2.fileResourceModule().fileResources().blockingAdd(file)
    }

    private fun saveDataElement(uid: String, value: String?): Flowable<StoreResult> {
        val valueRepository = d2.trackedEntityModule().trackedEntityDataValues()
            .value(recordUid, uid)
        val dataElement = d2.dataElementModule().dataElements().uid(uid).blockingGet()
        val valueType = dataElement?.valueType()
        val newValue = value.withValueTypeCheck(valueType) ?: ""

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet()?.value().withValueTypeCheck(valueType)
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
            EntryMode.DV,
            -> throw IllegalArgumentException(
                resourceManager.getString(R.string.data_values_save_error),
            )
        }
    }

    private fun deleteDataElementValue(field: String, optionUid: String): StoreResult {
        val option = d2.optionModule().options().uid(optionUid).blockingGet()
        val possibleValues = arrayListOf(option?.name(), option?.code()).filterNotNull()
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(recordUid, field)
        return if (valueRepository.blockingExists() &&
            possibleValues.contains(valueRepository.blockingGet()?.value())
        ) {
            saveDataElement(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun deleteAttributeValue(field: String, optionUid: String): StoreResult {
        val option = d2.optionModule().options().uid(optionUid).blockingGet()
        val possibleValues = arrayListOf(option?.name(), option?.code()).filterNotNull()
        val valueRepository =
            d2.trackedEntityModule().trackedEntityAttributeValues().value(field, recordUid)
        return if (valueRepository.blockingExists() &&
            possibleValues.contains(valueRepository.blockingGet()?.value())
        ) {
            saveAttribute(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    fun deleteOptionValueIfSelectedInGroup(
        field: String,
        optionGroupUid: String,
        isInGroup: Boolean,
    ): StoreResult {
        val optionsInGroup =
            d2.optionModule().optionGroups()
                .withOptions()
                .uid(optionGroupUid)
                .blockingGet()
                ?.options()
                ?.map { d2.optionModule().options().uid(it.uid()).blockingGet()?.code()!! }
                ?: arrayListOf()
        return when (entryMode) {
            EntryMode.DE -> deleteDataElementValueIfNotInGroup(
                field,
                optionsInGroup,
                isInGroup,
            )

            EntryMode.ATTR -> deleteAttributeValueIfNotInGroup(
                field,
                optionsInGroup,
                isInGroup,
            )

            EntryMode.DV,
            -> throw IllegalArgumentException(
                "DataValues can't be saved using these arguments. Use the other one.",
            )
        }
    }

    private fun deleteAttributeValueIfNotInGroup(
        field: String,
        optionCodesToShow: List<String>,
        isInGroup: Boolean,
    ): StoreResult {
        val valueRepository =
            d2.trackedEntityModule().trackedEntityAttributeValues().value(field, recordUid)
        return if (valueRepository.blockingExists() &&
            optionCodesToShow.contains(valueRepository.blockingGet()?.value()) == isInGroup
        ) {
            saveAttribute(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun deleteDataElementValueIfNotInGroup(
        field: String,
        optionCodesToShow: List<String>,
        isInGroup: Boolean,
    ): StoreResult {
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(recordUid, field)
        return if (valueRepository.blockingExists() &&
            optionCodesToShow.contains(valueRepository.blockingGet()?.value()) == isInGroup
        ) {
            saveDataElement(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }
}
