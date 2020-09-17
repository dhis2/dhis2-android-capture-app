package org.dhis2.data.search

import java.util.HashMap
import org.hisp.dhis.android.core.program.Program

data class SearchParametersModel(
    val selectedProgram: Program?,
    val trackedEntityType: String,
    val queryData: HashMap<String, String>?
) {
    fun copy(): SearchParametersModel = copy(
        queryData = hashMapOf<String, String>().apply { queryData?.let { putAll(it) } }
    )
}
