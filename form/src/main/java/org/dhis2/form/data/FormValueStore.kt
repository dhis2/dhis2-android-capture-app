package org.dhis2.form.data

import org.dhis2.form.model.StoreResult

interface FormValueStore {

    fun save(uid: String, value: String?, extraData: String?): StoreResult
}
