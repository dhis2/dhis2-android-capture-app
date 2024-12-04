package org.dhis2.form.data.metadata

import androidx.paging.PagingData
import androidx.paging.filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dhis2.commons.bindings.disableCollapsableSectionsInProgram
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option

open class FormBaseConfiguration(private val d2: D2) {
    fun optionGroups(optionGroupUids: List<String>) = d2.optionModule().optionGroups()
        .withOptions()
        .byUid().`in`(optionGroupUids)
        .blockingGet()

    fun disableCollapsableSectionsInProgram(programUid: String) =
        d2.disableCollapsableSectionsInProgram(programUid)

    fun dateFormatConfiguration() =
        d2.systemInfoModule().systemInfo().blockingGet()?.dateFormat()

    fun options(
        optionSetUid: String,
        query: String,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): Flow<PagingData<Option>> {
        return when {
            query.isEmpty() -> d2.optionModule()
                .options()
                .byOptionSetUid().eq(optionSetUid)
                .getPagingData(10)

            else ->
                d2.optionModule()
                    .options()
                    .byOptionSetUid().eq(optionSetUid)
                    .byDisplayName().like("%$query%")
                    .getPagingData(10)
        }.map { pagingData ->
            pagingData.filter { option ->

                val optionInGroupToHide = d2.optionModule().optionGroups()
                    .withOptions()
                    .byUid().`in`(optionGroupsToHide)
                    .blockingGet().any { optionGroup ->
                        optionGroup.options()?.map { it.uid() }?.contains(option.uid()) == true
                    }

                val optionInGroupToShow = d2.optionModule().optionGroups()
                    .withOptions()
                    .byUid().`in`(optionGroupsToShow)
                    .blockingGet().any { optionGroup ->
                        optionGroup.options()?.map { it.uid() }?.contains(option.uid()) == true
                    }

                val hideOption = if (optionGroupsToShow.isEmpty()) {
                    optionsToHide.contains(option.uid()) || optionInGroupToHide
                } else {
                    !optionInGroupToShow
                }

                !hideOption
            }
        }
    }
}
