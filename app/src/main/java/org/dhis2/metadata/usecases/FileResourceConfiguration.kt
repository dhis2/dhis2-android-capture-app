package org.dhis2.metadata.usecases

import org.hisp.dhis.android.core.D2

class FileResourceConfiguration(private val d2: D2) {
    fun download() {
        d2.fileResourceModule().fileResourceDownloader().blockingDownload()
    }
}
