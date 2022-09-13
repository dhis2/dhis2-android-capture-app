package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import java.io.File
import org.dhis2.Bindings.blockingSetCheck
import org.dhis2.Bindings.withValueTypeCheck
import org.dhis2.commons.data.DataEntryStore
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository

class ValueStoreImpl(
    private val d2: D2,
    private val recordUid: String,
    private val entryMode: DataEntryStore.EntryMode,
    private val dhisEnrollmentUtils: DhisEnrollmentUtils,
    private val crashReportController: CrashReportController,
    private val networkUtils: NetworkUtils,
    private val searchTEIRepository: SearchTEIRepository,
    private val fieldErrorMessageProvider: FieldErrorMessageProvider
) : ValueStore {
    var enrollmentRepository: EnrollmentObjectRepository? = null
    var overrideProgramUid: String? = null

    constructor(
        d2: D2,
        recordUid: String,
        entryMode: DataEntryStore.EntryMode,
        dhisEnrollmentUtils: DhisEnrollmentUtils,
        enrollmentRepository: EnrollmentObjectRepository,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
        searchTEIRepository: SearchTEIRepository,
        fieldErrorMessageProvider: FieldErrorMessageProvider
    ) : this(
        d2,
        recordUid,
        entryMode,
        dhisEnrollmentUtils,
        crashReportController,
        networkUtils,
        searchTEIRepository,
        fieldErrorMessageProvider
    ) {
        this.enrollmentRepository = enrollmentRepository
    }

    override fun overrideProgram(programUid: String?) {
        overrideProgramUid = programUid
    }

    override fun save(uid: String, value: String?): Flowable<StoreResult> {
        return when (entryMode) {
            DataEntryStore.EntryMode.DE -> saveDataElement(uid, value)
            DataEntryStore.EntryMode.ATTR -> saveAttribute(uid, value)
            DataEntryStore.EntryMode.DV ->
                throw IllegalArgumentException(
                    "DataValues can't be saved using these arguments. Use the other one."
                )
        }
    }

    override fun save(dataValue: DataSetTableModel): Flowable<StoreResult> {
        val dataValueObject = d2.dataValueModule().dataValues().value(
            dataValue.period,
            dataValue.organisationUnit,
            dataValue.dataElement,
            dataValue.categoryOptionCombo,
            dataValue.attributeOptionCombo
        )

        val validator = d2.dataElementModule().dataElements()
            .uid(dataValue.dataElement).blockingGet().valueType()?.validator

        return if (!dataValue.value.isNullOrEmpty()) {
            if (dataValueObject.blockingExists() &&
                dataValueObject.blockingGet().value() == dataValue.value
            ) {
                Flowable.just(StoreResult("", ValueStoreResult.VALUE_HAS_NOT_CHANGED))
            } else {
                when (val validation = validator?.validate(dataValue.value)) {
                    is Result.Failure -> Flowable.just(
                        StoreResult(
                            uid = "",
                            valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
                            valueStoreResultMessage = fieldErrorMessageProvider
                                .getFriendlyErrorMessage(validation.failure)
                        )
                    )
                    is Result.Success ->
                        dataValueObject.set(dataValue.value)
                            .andThen(Flowable.just(StoreResult("", ValueStoreResult.VALUE_CHANGED)))
                    else -> Flowable.just(
                        StoreResult(
                            uid = "",
                            valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
                            valueStoreResultMessage = fieldErrorMessageProvider
                                .defaultValidationErrorMessage()
                        )
                    )
                }
            }
        } else {
            dataValueObject.deleteIfExist()
                .andThen(Flowable.just(StoreResult("", ValueStoreResult.VALUE_CHANGED)))
        }
    }

    override fun saveWithTypeCheck(uid: String, value: String?): Flowable<StoreResult> {
        return when {
            d2.dataElementModule().dataElements().uid(uid).blockingExists() ->
                saveDataElement(uid, value)
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingExists() ->
                saveAttribute(uid, value)
            else -> Flowable.just(StoreResult(uid, ValueStoreResult.UID_IS_NOT_DE_OR_ATTR))
        }
    }

    private fun saveAttribute(uid: String, value: String?): Flowable<StoreResult> {
        val teiUid =
            when (entryMode) {
                DataEntryStore.EntryMode.DE -> {
                    val event = d2.eventModule().events().uid(recordUid).blockingGet()
                    val enrollment = d2.enrollmentModule().enrollments()
                        .uid(event.enrollment()).blockingGet()
                    enrollment.trackedEntityInstance()
                }
                DataEntryStore.EntryMode.ATTR -> recordUid
                DataEntryStore.EntryMode.DV -> null
            }
                ?: return Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED))

        if (!checkUniqueFilter(uid, value, teiUid)) {
            return Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_NOT_UNIQUE))
        }

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(uid, teiUid)
        val valueType =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet().valueType()
        var newValue = value.withValueTypeCheck(valueType) ?: ""
        if (valueType == ValueType.IMAGE && value != null) {
            newValue = saveFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value().withValueTypeCheck(valueType)
        } else {
            ""
        }
        return if (currentValue != newValue) {
            if (!DhisTextUtils.isEmpty(value)) {
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

    private fun saveDataElement(uid: String, value: String?): Flowable<StoreResult> {
        val valueRepository = d2.trackedEntityModule().trackedEntityDataValues()
            .value(recordUid, uid)
        val valueType = d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType()
        var newValue = value.withValueTypeCheck(valueType) ?: ""
        if (valueType == ValueType.IMAGE && value != null) {
            newValue = saveFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value().withValueTypeCheck(valueType)
        } else {
            ""
        }

        return if (currentValue != newValue) {
            if (!DhisTextUtils.isEmpty(value)) {
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

    private fun checkUniqueFilter(uid: String, value: String?, teiUid: String): Boolean {
        return if (!networkUtils.isOnline()) {
            dhisEnrollmentUtils.isTrackedEntityAttributeValueUnique(uid, value, teiUid)
        } else {
            val programUid = overrideProgramUid ?: enrollmentRepository?.blockingGet()?.program()
            searchTEIRepository.isUniqueTEIAttributeOnline(uid, value, teiUid, programUid)
        }
    }

    private fun saveFileResource(path: String): String {
        val file = FileResizerHelper.resizeFile(File(path), FileResizerHelper.Dimension.MEDIUM)
        return d2.fileResourceModule().fileResources().blockingAdd(file)
    }

    override fun deleteOptionValueIfSelected(field: String, optionUid: String): StoreResult {
        return when (entryMode) {
            DataEntryStore.EntryMode.DE -> deleteDataElementValue(field, optionUid)
            DataEntryStore.EntryMode.ATTR -> deleteAttributeValue(field, optionUid)
            DataEntryStore.EntryMode.DV
            -> throw IllegalArgumentException(
                "DataValues can't be saved using these arguments. Use the other one."
            )
        }
    }

    override fun deleteOptionValueIfSelectedInGroup(
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
            DataEntryStore.EntryMode.DE -> deleteDataElementValueIfNotInGroup(
                field,
                optionsInGroup,
                isInGroup
            )
            DataEntryStore.EntryMode.ATTR -> deleteAttributeValueIfNotInGroup(
                field,
                optionsInGroup,
                isInGroup
            )
            DataEntryStore.EntryMode.DV
            -> throw IllegalArgumentException(
                "DataValues can't be saved using these arguments. Use the other one."
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
            save(field, null).blockingFirst()
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
            save(field, null).blockingFirst()
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
            save(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
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
            save(field, null).blockingFirst()
        } else {
            StoreResult(field, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    override fun deleteOptionValues(optionCodeValuesToDelete: List<String>) {
        when (entryMode) {
            DataEntryStore.EntryMode.DE -> deleteOptionValuesForEvents(optionCodeValuesToDelete)
            DataEntryStore.EntryMode.ATTR -> deleteOptionValuesForEnrollment(
                optionCodeValuesToDelete
            )
            DataEntryStore.EntryMode.DV
            -> throw IllegalArgumentException(
                "DataValues can't be saved using these arguments. Use the other one."
            )
        }
    }

    private fun deleteOptionValuesForEvents(optionCodeValuesToDelete: List<String>) {
        d2.trackedEntityModule().trackedEntityDataValues()
            .byEvent().eq(recordUid)
            .byValue().`in`(optionCodeValuesToDelete)
            .blockingGet().filter {
                d2.dataElementModule().dataElements()
                    .uid(it.dataElement())
                    .blockingGet()
                    .optionSetUid() != null
            }.forEach {
                saveDataElement(it.dataElement()!!, null)
            }
    }

    private fun deleteOptionValuesForEnrollment(optionCodeValuesToDelete: List<String>) {
        d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().eq(recordUid)
            .byValue().`in`(optionCodeValuesToDelete)
            .blockingGet().filter {
                d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(it.trackedEntityAttribute()).blockingGet().optionSet()?.uid() != null
            }.forEach {
                saveAttribute(it.trackedEntityAttribute()!!, null)
            }
    }
}
