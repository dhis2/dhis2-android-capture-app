package org.dhis2.mobile.aggregates.data

import androidx.paging.map
import kotlinx.coroutines.flow.map
import org.dhis2.commons.bindings.dataElement
import org.dhis2.mobile.aggregates.model.OptionData
import org.hisp.dhis.android.core.D2

class OptionRepositoryImpl(private val d2: D2) : OptionRepository {
    override suspend fun optionCount(dataElementUid: String): Int {
        return d2.dataElement(dataElementUid)?.optionSet()?.uid()?.let { optionSetUid ->
            d2.optionModule().options()
                .byOptionSetUid().eq(optionSetUid)
                .blockingCount()
        } ?: 0
    }

    override suspend fun options(dataElementUid: String): List<OptionData> {
        val optionSetUid = d2.dataElement(dataElementUid)?.optionSet()?.uid() ?: return emptyList()
        val optionFlow = d2.optionModule().options()
            .byOptionSetUid().eq(optionSetUid)
            .blockingGet()
            .map { option ->
                OptionData(
                    uid = option.code() ?: option.uid(),
                    code = option.code(),
                    label = option.displayName() ?: option.uid(),
                )
            }

        return optionFlow
    }
}
