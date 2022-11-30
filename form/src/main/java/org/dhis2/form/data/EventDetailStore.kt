package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.form.model.StoreResult

class EventDetailStore : FormValueStore {
    override fun save(uid: String, value: String?, extraData: String?): StoreResult {
        return StoreResult("")
    }

    override fun saveWithTypeCheck(uid: String, value: String?): Flowable<StoreResult> {
        return Flowable.just(StoreResult(""))
    }

    override fun deleteOptionValues(optionCodeValuesToDelete: List<String>) {
    }

    override fun deleteOptionValueIfSelected(field: String, optionUid: String): StoreResult {
        return StoreResult("")
    }

    override fun deleteOptionValueIfSelectedInGroup(
        field: String,
        optionGroupUid: String,
        isInGroup: Boolean
    ): StoreResult {
        return StoreResult("")
    }
}
