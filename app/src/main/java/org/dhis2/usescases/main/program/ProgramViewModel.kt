package org.dhis2.usescases.main.program

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.common.State

data class ProgramViewModel(
    val uid: String,
    val title: String,
    val metadataIconData: MetadataIconData,
    val count: Int,
    val type: String?,
    val typeName: String,
    val programType: String,
    val description: String?,
    val onlyEnrollOnce: Boolean,
    val accessDataWrite: Boolean,
    val state: State,
    val hasOverdueEvent: Boolean,
    val filtersAreActive: Boolean,
    val downloadState: ProgramDownloadState,
    val downloadActive: Boolean = false
) {
    private var hasShownCompleteSyncAnimation = false

    fun setCompleteSyncAnimation() {
        hasShownCompleteSyncAnimation = true
    }

    fun hasShowCompleteSyncAnimation() = hasShownCompleteSyncAnimation

    fun translucent(): Boolean {
        return (filtersAreActive && count == 0) || downloadState == ProgramDownloadState.DOWNLOADING
    }

    fun countDescription() = "%s %s".format(count, typeName)

    fun isDownloading() = downloadActive || downloadState == ProgramDownloadState.DOWNLOADING

    fun getAlphaValue() = if (isDownloading()) {
        0.5f
    } else {
        1f
    }
}

enum class ProgramDownloadState {
    DOWNLOADING, DOWNLOADED, ERROR, NONE
}
