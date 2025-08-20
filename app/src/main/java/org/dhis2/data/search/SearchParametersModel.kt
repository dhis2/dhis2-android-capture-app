package org.dhis2.data.search

import org.hisp.dhis.android.core.program.Program

data class SearchParametersModel(
    val selectedProgram: Program?,
    val queryData: MutableMap<String, List<String>?>?,
) {
    fun copy(): SearchParametersModel =
        copy(
            queryData = mutableMapOf<String, List<String>?>().apply { queryData?.let { putAll(it) } },
        )
}
