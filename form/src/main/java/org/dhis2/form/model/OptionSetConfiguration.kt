package org.dhis2.form.model

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.option.Option

data class OptionSetConfiguration(
    val searchEmitter: MutableStateFlow<String>? = null,
    val optionFlow: Flow<PagingData<OptionData>>,
) {
    companion object {
        fun optionDataFlow(
            flow: Flow<PagingData<Option>>,
            fetchMetadataIconData: (option: Option) -> MetadataIconData,
        ) =
            flow.map { pagingData ->
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
