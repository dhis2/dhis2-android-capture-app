package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.form.model.StoreResult

interface FormValueStore {

    fun save(uid: String, value: String?, extraData: String?): Flowable<StoreResult>
}
