package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryOption

class ConfigureEventCatCombo(
    val repository: EventDetailsRepository,
) {

    private var selectedCategoryOptions = mapOf<String, CategoryOption?>()

    operator fun invoke(categoryOption: Pair<String, String?>? = null): Flow<EventCatCombo> {
        repository.catCombo().apply {
            val categories = getCategories(this?.categories())
            val categoryOptions = getCategoryOptions()
            updateSelectedOptions(categoryOption, categories, categoryOptions)
            val catComboUid = getCatComboUid(this?.uid() ?: "", this?.isDefault ?: false)
            val catComboDisplayName = getCatComboDisplayName(this?.uid() ?: "")

            return flowOf(
                EventCatCombo(
                    uid = catComboUid,
                    isDefault = this?.isDefault ?: false,
                    categories = categories,
                    categoryOptions = categoryOptions,
                    selectedCategoryOptions = selectedCategoryOptions,
                    isCompleted = isCompleted(
                        isDefault = this?.isDefault ?: true,
                        categories = this?.categories(),
                        selectedCategoryOptions = selectedCategoryOptions,
                    ),
                    displayName = catComboDisplayName,
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
            ).firstOrNull()?.uid()
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

    private fun getCatComboDisplayName(categoryComboUid: String): String? {
        return repository.getCatOptionComboDisplayName(categoryComboUid)
    }

    private fun updateSelectedOptions(
        categoryOption: Pair<String, String?>?,
        categories: List<EventCategory>,
        categoryOptions: Map<String, CategoryOption>?,
    ): Map<String, CategoryOption?> {
        if (categoryOption == null) {
            categories.forEach { category ->
                categoryOptions?.get(category.uid)?.let { categoryOption ->
                    val copy = selectedCategoryOptions.toMutableMap()
                    copy[category.uid] = categoryOption
                    selectedCategoryOptions = copy
                }
            }
        } else {
            categoryOption.let { pair ->
                val copy = selectedCategoryOptions.toMutableMap()
                copy[pair.first] = pair.second?.let { categoryOptionId ->
                    repository.getCatOption(categoryOptionId)
                }
                selectedCategoryOptions = copy
            }
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
