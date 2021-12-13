package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.form.model.StoreResult

interface FormValueStore {

    fun save(uid: String, value: String?, extraData: String?): StoreResult
    fun saveWithTypeCheck(uid: String, value: String?): Flowable<StoreResult>
    fun deleteOptionValues(optionCodeValuesToDelete: List<String>)
    fun deleteOptionValueIfSelected(field: String, optionUid: String): StoreResult
    fun deleteOptionValueIfSelectedInGroup(
        field: String,
        optionGroupUid: String,
        isInGroup: Boolean
    ): StoreResult
}
