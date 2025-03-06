package org.dhis2.mobile.aggregates.data

import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.map
import kotlinx.coroutines.flow.map
import org.dhis2.commons.bindings.dataElement
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData

class OptionRepositoryImpl(private val d2: D2) : OptionRepository {

    @Composable
    override fun fetchOptionsMap(dataElementUid: String, selectedOptionCodes: List<String>): Map<String, CheckBoxData> {
        val optionSetUid = d2.dataElement(dataElementUid)?.optionSet()?.uid() ?: return emptyMap()
        val pageFlow = d2.optionModule().options()
            .byOptionSetUid()
            .eq(optionSetUid)
            .getPagingData(20)
            .map { pagingData ->
                pagingData.map { option ->
                    _root_ide_package_.org.dhis2.mobile.aggregates.model.OptionData(
                        uid = option.uid(),
                        code = option.code(),
                        label = option.displayName() ?: option.uid(),
                    )
                }
            }

        return buildMap {
            pageFlow.collectAsLazyPagingItems().let { paging ->
                repeat(paging.itemCount) { index ->
                    val optionData = paging[index]
                    put(
                        optionData?.code ?: "",
                        CheckBoxData(
                            uid = optionData?.uid ?: "",
                            checked = optionData?.code?.let {
                                selectedOptionCodes.contains(it)
                            } ?: false,
                            enabled = true,
                            textInput = optionData?.label ?: "",
                        ),
                    )
                }
            }
        }
    }
}
