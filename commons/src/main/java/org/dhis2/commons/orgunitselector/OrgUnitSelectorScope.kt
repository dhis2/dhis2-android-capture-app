package org.dhis2.commons.orgunitselector

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class OrgUnitSelectorScope(val uid: String?) : Parcelable {
    data class UserSearchScope(val userUid: String? = null) : OrgUnitSelectorScope(userUid)
    data class UserCaptureScope(val userUid: String? = null) : OrgUnitSelectorScope(userUid)
    data class ProgramSearchScope(val programUid: String) : OrgUnitSelectorScope(programUid)
    data class ProgramCaptureScope(val programUid: String) : OrgUnitSelectorScope(programUid)
    data class DataSetSearchScope(val dataSetUid: String) : OrgUnitSelectorScope(dataSetUid)
    data class DataSetCaptureScope(val dataSetUid: String) : OrgUnitSelectorScope(dataSetUid)
}
