package org.dhis2.form.data.metadata

import org.dhis2.bindings.fileResourceNameOf
import org.hisp.dhis.android.core.D2

class FileResourceConfiguration(
    val d2: D2,
) {
    fun getFilePath(uid: String): String? =
        if (d2
                .fileResourceModule()
                .fileResources()
                .uid(uid)
                .blockingExists()
        ) {
            d2
                .fileResourceModule()
                .fileResources()
                .uid(uid)
                .blockingGet()
                ?.path()
        } else {
            null
        }

    fun getFileName(uidOrPath: String): String? = d2.fileResourceNameOf(uidOrPath)
}
