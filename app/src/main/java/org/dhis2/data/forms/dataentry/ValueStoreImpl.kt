package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import java.io.File
import org.dhis2.Bindings.blockingSetCheck
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper
import org.hisp.dhis.android.core.common.ValueType

class ValueStoreImpl(
    private val d2: D2,
    private val recordUid: String,
    private val entryMode: DataEntryStore.EntryMode
) : ValueStore {

    enum class ValueStoreResult {
        VALUE_CHANGED,
        VALUE_HAS_NOT_CHANGED,
        VALUE_NOT_UNIQUE,
        UID_IS_NOT_DE_OR_ATTR
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
            dataValue.period(),
            dataValue.organisationUnit(),
            dataValue.dataElement(),
            dataValue.categoryOptionCombo(),
            dataValue.attributeOptionCombo()
        )

        return if (!dataValue.value().isNullOrEmpty()) {
            if (dataValueObject.blockingExists() &&
                dataValueObject.blockingGet().value() == dataValue.value()
            ) {
                Flowable.just(StoreResult("", ValueStoreResult.VALUE_HAS_NOT_CHANGED))
            } else {
                dataValueObject.set(dataValue.value())
                    .andThen(Flowable.just(StoreResult("", ValueStoreResult.VALUE_CHANGED)))
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
        if (!checkUniqueFilter(uid, value)) {
            return Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_NOT_UNIQUE))
        }

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(uid, recordUid)
        var newValue = value ?: ""
        if (d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet().valueType() ==
            ValueType.IMAGE &&
            value != null
        ) {
            newValue = saveFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value()
        } else {
            ""
        }
        return if (currentValue != newValue) {
            if (!DhisTextUtils.isEmpty(value)) {
                valueRepository.blockingSetCheck(d2, uid, newValue)
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
        var newValue = value ?: ""
        if (d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType() ==
            ValueType.IMAGE &&
            value != null
        ) {
            newValue = saveFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value()
        } else {
            ""
        }

        return if (currentValue != newValue) {
            if (!DhisTextUtils.isEmpty(value)) {
                valueRepository.blockingSetCheck(d2, uid, newValue)
            } else {
                valueRepository.blockingDeleteIfExist()
            }
            Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_CHANGED))
        } else {
            Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED))
        }
    }

    private fun checkUniqueFilter(uid: String, value: String?): Boolean {
        return if (value != null) {
            val isUnique =
                d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!.unique()
                    ?: false
            if (isUnique) {
                val hasValue = d2.trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityAttribute().eq(uid)
                    .byValue().eq(value).blockingGet().isNotEmpty()
                !hasValue
            } else {
                true
            }
        } else {
            true
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
