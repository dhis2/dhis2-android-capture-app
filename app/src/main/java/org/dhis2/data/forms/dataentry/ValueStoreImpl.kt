package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import java.io.File
import org.dhis2.Bindings.blockingSetCheck
import org.dhis2.Bindings.withValueTypeCheck
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.form.R
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository

class ValueStoreImpl(
    private val d2: D2,
    private val recordUid: String,
    private val entryMode: EntryMode,
    private val dhisEnrollmentUtils: DhisEnrollmentUtils,
    private val crashReportController: CrashReportController,
    private val networkUtils: NetworkUtils,
    private val searchTEIRepository: SearchTEIRepository,
    private val fieldErrorMessageProvider: FieldErrorMessageProvider,
    private val resourceManager: ResourceManager
) : ValueStore {
    var enrollmentRepository: EnrollmentObjectRepository? = null
    var overrideProgramUid: String? = null

    override fun overrideProgram(programUid: String?) {
        overrideProgramUid = programUid
    }

    override fun validate(dataElementUid: String, value: String?): Result<String, Throwable> {
        if (value.isNullOrEmpty()) return Result.Success("")
        val dataElement = d2.dataElementModule()
            .dataElements()
            .uid(dataElementUid)
            .blockingGet()
        return dataElement.valueType()?.validator?.validate(value) ?: Result.Success("")
    }

    override fun save(uid: String, value: String?): Flowable<StoreResult> {
        return when (entryMode) {
            EntryMode.DE -> saveDataElement(uid, value)
            EntryMode.ATTR -> saveAttribute(uid, value)
            EntryMode.DV ->
                throw IllegalArgumentException(
                    resourceManager.getString(R.string.data_values_save_error)
                )
        }
    }

    override fun save(
        orgUnitUid: String,
        periodId: String,
        attributeOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
        value: String?
    ): Flowable<StoreResult> {
        val dataValueObject = d2.dataValueModule().dataValues().value(
            periodId,
            orgUnitUid,
            dataElementUid,
            categoryOptionComboUid,
            attributeOptionComboUid
        )

        val validator = d2.dataElementModule().dataElements()
            .uid(dataElementUid).blockingGet().valueType()?.validator

        return if (!value.isNullOrEmpty()) {
            if (dataValueObject.blockingExists() &&
                dataValueObject.blockingGet().value() == value
            ) {
                Flowable.just(StoreResult("", ValueStoreResult.VALUE_HAS_NOT_CHANGED))
            } else {
                when (val validation = validator?.validate(value)) {
                    is Result.Failure -> Flowable.just(
                        StoreResult(
                            uid = "",
                            valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
                            valueStoreResultMessage = fieldErrorMessageProvider
                                .getFriendlyErrorMessage(validation.failure)
                        )
                    )
                    is Result.Success ->
                        dataValueObject.set(value)
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
            if (dataValueObject.blockingExists()) {
                dataValueObject.deleteIfExist()
                    .andThen(Flowable.just(StoreResult("", ValueStoreResult.VALUE_CHANGED)))
            } else {
                Flowable.just(StoreResult("", ValueStoreResult.VALUE_HAS_NOT_CHANGED))
            }
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
        val attr = d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()
        val valueType = attr.valueType()
        val optionSet = attr.optionSet()
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
        val de = d2.dataElementModule().dataElements().uid(uid).blockingGet()
        val valueType = de.valueType()
        val optionSet = de.optionSet()
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

    private fun saveFileResource(path: String, resize: Boolean): String {
        val file = if (resize) {
            FileResizerHelper.resizeFile(File(path), FileResizerHelper.Dimension.MEDIUM)
        } else {
            File(path)
        }
        return d2.fileResourceModule().fileResources().blockingAdd(file)
    }

    override fun deleteOptionValueIfSelected(field: String, optionUid: String): StoreResult {
        return when (entryMode) {
            EntryMode.DE -> deleteDataElementValue(field, optionUid)
            EntryMode.ATTR -> deleteAttributeValue(field, optionUid)
            EntryMode.DV
            -> throw IllegalArgumentException(
                resourceManager.getString(R.string.data_values_save_error)
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
            EntryMode.DE -> deleteOptionValuesForEvents(optionCodeValuesToDelete)
            EntryMode.ATTR -> deleteOptionValuesForEnrollment(
                optionCodeValuesToDelete
            )
            EntryMode.DV
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

    private fun isFile(valueType: ValueType?): Boolean {
        return valueType == ValueType.IMAGE || valueType?.isFile == true
    }
}
