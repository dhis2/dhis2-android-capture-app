package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import org.dhis2.Bindings.blockingSetCheck
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import java.io.File

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
            DataEntryStore.EntryMode.DV -> throw IllegalArgumentException("DavaValues can't be saved using these arguments. Use the other one.")
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
            if (dataValueObject.blockingExists() && dataValueObject.blockingGet().value() == dataValue.value()) {
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
            d2.dataElementModule().dataElements().uid(uid).blockingExists() -> saveDataElement(
                uid,
                value
            )
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingExists() -> saveAttribute(
                uid,
                value
            )
            else -> Flowable.just(StoreResult(uid, ValueStoreResult.UID_IS_NOT_DE_OR_ATTR))
        }
    }

    private fun saveAttribute(uid: String, value: String?): Flowable<StoreResult> {

        if (!checkUniqueFilter(uid, value)) {
            return Flowable.just(StoreResult(uid, ValueStoreResult.VALUE_NOT_UNIQUE))
        }

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(uid, recordUid)
        var newValue = value?:""
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
                valueRepository.blockingSetCheck(d2, uid, newValue!!)
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
        var newValue = value?:""
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
                valueRepository.blockingSetCheck(d2, uid, newValue!!)
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
            val hasValue = d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq(uid)
                .byValue().eq(value).blockingGet().isNotEmpty()
            if (isUnique) {
                !hasValue
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun saveFileResource(path: String): String {
        val file = File(path)
        return d2.fileResourceModule().fileResources().blockingAdd(file)
    }

}