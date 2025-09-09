package org.dhis2.form.model

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.dhis2.mobile.commons.model.MetadataIconData
import org.hisp.dhis.android.core.option.Option

data class OptionSetConfiguration(
    val searchEmitter: StateFlow<String>? = null,
    val onSearch: (String) -> Unit,
    val optionFlow: Flow<PagingData<OptionData>>,
) {
    companion object {
        fun optionDataFlow(
            flow: Flow<PagingData<Option>>,
            fetchMetadataIconData: (option: Option) -> MetadataIconData,
        ) = flow.map { pagingData ->
            pagingData.map { option ->
                OptionData(
                    option = option,
                    metadataIconData = fetchMetadataIconData(option),
                )
            }
        }
    }

    data class OptionData(
        val option: Option,
        val metadataIconData: MetadataIconData,
    )
}
