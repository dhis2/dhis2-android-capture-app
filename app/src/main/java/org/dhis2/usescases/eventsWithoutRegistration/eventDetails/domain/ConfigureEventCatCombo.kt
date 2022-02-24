package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption

class ConfigureEventCatCombo(
    val eventInitialRepository: EventInitialRepository,
    val programUid: String,
    val eventUid: String?
) {

    private var selectedCategoryOptions = mapOf<String, CategoryOption?>()

    operator fun invoke(categoryOption: Pair<String, String?>? = null): Flow<EventCatCombo> {
        categoryOption?.let {
            updateSelectedOptions(it)
        }
        getCategoryCombo().apply {
            return flowOf(
                EventCatCombo(
                    uid = getCatComboUid(uid(), isDefault ?: false),
                    isDefault = isDefault ?: false,
                    categories = getCategories(categories()),
                    categoryOptions = getCategoryOptions(),
                    selectedCategoryOptions = selectedCategoryOptions,
                    isCompleted = isCompleted(
                        isDefault = isDefault ?: true,
                        categories = categories(),
                        selectedCategoryOptions = selectedCategoryOptions
                    )
                )
            )
        }
    }

    private fun isCompleted(
        isDefault: Boolean,
        categories: List<Category>?,
        selectedCategoryOptions: Map<String, CategoryOption?>?
    ): Boolean {
        return if (isDefault) {
            true
        } else {
            !categories.isNullOrEmpty() && selectedCategoryOptions?.size == categories.size
        }
    }

    private fun getCatComboUid(categoryComboUid: String, isDefault: Boolean): String? {
        eventUid?.let {
            return eventInitialRepository.event(it).blockingFirst().attributeOptionCombo()
        }

        if (isDefault) {
            return eventInitialRepository.catOptionCombos(
                categoryComboUid
            ).blockingFirst().first().uid()
        }

        val valuesList = getUidsList(selectedCategoryOptions.values.filterNotNull())
        if (valuesList.isNotEmpty()) {
            return eventInitialRepository.getCategoryOptionCombo(
                categoryComboUid,
                valuesList
            )
        }
        return null
    }

    private fun updateSelectedOptions(categoryOption: Pair<String, String?>?): Map<String, CategoryOption?> {
        categoryOption?.let { pair ->
            val copy = selectedCategoryOptions.toMutableMap()
            copy[pair.first] = pair.second?.let { categoryOptionId ->
                eventInitialRepository.getCatOption(categoryOptionId)
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
                optionsSize = eventInitialRepository.getCatOptionSize(category.uid()),
                options = eventInitialRepository.getCategoryOptions(category.uid())
            )
        } ?: emptyList()
    }

    private fun getCategoryOptions(): Map<String, CategoryOption>? {
        return eventUid?.let {
            eventInitialRepository.getOptionsFromCatOptionCombo(it).blockingFirst()
        }
    }

    private fun getCategoryCombo(): CategoryCombo {
        return eventInitialRepository.catCombo(programUid).blockingFirst()
    }
}
