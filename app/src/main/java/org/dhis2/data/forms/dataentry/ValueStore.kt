package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import org.dhis2.form.model.StoreResult
import org.hisp.dhis.android.core.arch.helpers.Result as ValidatorResult

interface ValueStore {
    fun save(uid: String, value: String?): Flowable<StoreResult>
    fun saveWithTypeCheck(uid: String, value: String?): Flowable<StoreResult>
    fun save(
        orgUnitUid: String,
        periodId: String,
        attributeOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
        value: String?,
    ): Flowable<StoreResult>

    fun deleteOptionValues(optionCodeValuesToDelete: List<String>)
    fun deleteOptionValueIfSelected(field: String, optionUid: String): StoreResult
    fun deleteOptionValueIfSelectedInGroup(
        field: String,
        optionGroupUid: String,
        isInGroup: Boolean,
    ): StoreResult

    fun overrideProgram(programUid: String?)
    fun validate(dataElementUid: String, value: String?): ValidatorResult<String, Throwable>
}
