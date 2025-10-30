package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.OptionData

interface OptionRepository {
    suspend fun optionCount(dataElementUid: String): Int

    suspend fun options(dataElementUid: String): List<OptionData>
}
