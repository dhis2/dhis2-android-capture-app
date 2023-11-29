package org.dhis2.usescases.eventswithoutregistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.usescases.eventswithoutregistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventswithoutregistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventswithoutregistration.eventDetails.models.EventCategory
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryOption

class ConfigureEventCatCombo(
    val repository: EventDetailsRepository,
) {

    private var selectedCategoryOptions = mapOf<String, CategoryOption?>()

    operator fun invoke(categoryOption: Pair<String, String?>? = null): Flow<EventCatCombo> {
        categoryOption?.let {
            updateSelectedOptions(it)
        }
        repository.catCombo().apply {
            return flowOf(
                EventCatCombo(
                    uid = getCatComboUid(this?.uid() ?: "", this?.isDefault ?: false),
                    isDefault = this?.isDefault ?: false,
                    categories = getCategories(this?.categories()),
                    categoryOptions = getCategoryOptions(),
                    selectedCategoryOptions = selectedCategoryOptions,
                    isCompleted = isCompleted(
                        isDefault = this?.isDefault ?: true,
                        categories = this?.categories(),
                        selectedCategoryOptions = selectedCategoryOptions,
                    ),
                ),
            )
        }
    }

    private fun isCompleted(
        isDefault: Boolean,
        categories: List<Category>?,
        selectedCategoryOptions: Map<String, CategoryOption?>?,
    ): Boolean {
        return if (isDefault) {
            true
        } else {
            !categories.isNullOrEmpty() && selectedCategoryOptions?.size == categories.size
        }
    }

    private fun getCatComboUid(categoryComboUid: String, isDefault: Boolean): String? {
        if (isDefault) {
            return repository.getCatOptionCombos(
                categoryComboUid,
            ).first().uid()
        }

        val valuesList = getUidsList(selectedCategoryOptions.values.filterNotNull())
        if (valuesList.isNotEmpty()) {
            return repository.getCategoryOptionCombo(
                categoryComboUid,
                valuesList,
            )
        }

        repository.getEvent()?.let {
            return it.attributeOptionCombo()
        }

        return null
    }

    private fun updateSelectedOptions(
        categoryOption: Pair<String, String?>?,
    ): Map<String, CategoryOption?> {
        categoryOption?.let { pair ->
            val copy = selectedCategoryOptions.toMutableMap()
            copy[pair.first] = pair.second?.let { categoryOptionId ->
                repository.getCatOption(categoryOptionId)
            }
            selectedCategoryOptions = copy
        }
        return selectedCategoryOptions
    }

    private fun getCategories(categories: MutableList<Category>?): List<EventCategory> {
        return categories?.map { category ->
            EventCategory(
                uid = category.uid(),
                name = category.displayName() ?: category.uid(),
                optionsSize = repository.getCatOptionSize(category.uid()),
                options = repository.getCategoryOptions(category.uid()),
            )
        } ?: emptyList()
    }

    private fun getCategoryOptions(): Map<String, CategoryOption>? {
        return repository.getOptionsFromCatOptionCombo()
    }
}
