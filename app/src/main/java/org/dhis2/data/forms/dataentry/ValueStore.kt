package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import java.io.File

class ValueStore(
    private val d2: D2,
    private val recordUid: String,
    private val entryMode: DataEntryStore.EntryMode
) {

    enum class ValueStoreResult {
        VALUE_CHANGED,
        VALUE_HAS_NOT_CHANGED,
        VALUE_NOT_UNIQUE
    }

    fun save(uid: String, value: String?): Flowable<ValueStoreResult> {
        return when (entryMode) {
            DataEntryStore.EntryMode.DE -> saveDataElement(uid, value)
            DataEntryStore.EntryMode.ATTR -> saveAttribute(uid, value)
        }
    }

    private fun saveAttribute(uid: String, value: String?): Flowable<ValueStoreResult> {

        if(!checkUniqueFilter(uid,value)){
            return Flowable.just(ValueStoreResult.VALUE_NOT_UNIQUE)
        }

        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(uid, recordUid)
        var newValue = value
        if (d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet().valueType() ==
            ValueType.IMAGE &&
            value != null
        ) {
            newValue = getFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value()
        } else {
            null
        }

        return if (currentValue != newValue) {
            if (!DhisTextUtils.isEmpty(value)) {
                valueRepository.blockingSet(newValue)
            } else {
                valueRepository.blockingDelete()
            }
            Flowable.just(ValueStoreResult.VALUE_CHANGED)
        } else {
            Flowable.just(ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun saveDataElement(uid: String, value: String?): Flowable<ValueStoreResult> {
        val valueRepository = d2.trackedEntityModule().trackedEntityDataValues()
            .value(recordUid,uid)
        var newValue = value
        if (d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType() ==
            ValueType.IMAGE &&
            value != null
        ) {
            newValue = getFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            valueRepository.blockingGet().value()
        } else {
            null
        }

        return if (currentValue != newValue) {
            if (!DhisTextUtils.isEmpty(value)) {
                valueRepository.blockingSet(newValue)
            } else {
                valueRepository.blockingDelete()
            }
            Flowable.just(ValueStoreResult.VALUE_CHANGED)
        } else {
            Flowable.just(ValueStoreResult.VALUE_HAS_NOT_CHANGED)
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

    private fun getFileResource(path: String): String {
        val file = File(path)
        return d2.fileResourceModule().fileResources().blockingAdd(file)
    }
}