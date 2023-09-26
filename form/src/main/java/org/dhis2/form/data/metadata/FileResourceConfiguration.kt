package org.dhis2.form.data.metadata

import org.hisp.dhis.android.core.D2

class FileResourceConfiguration(val d2: D2) {

    fun getFilePath(uid: String): String? {
        return if (d2.fileResourceModule().fileResources().uid(uid).blockingExists()) {
            d2.fileResourceModule().fileResources().uid(uid).blockingGet()?.path()
        } else {
            null
        }
    }
}
