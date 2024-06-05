package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.event.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConfigureEventCatComboTest {

    private val repository: EventDetailsRepository = mock()
    private val category: Category = mock {
        on { uid() } doReturn CATEGORY_UID
    }
    private val categoryCombo: CategoryCombo = mock {
        on { uid() } doReturn CATEGORY_COMBO_UID
        on { categories() } doReturn listOf(category)
    }
    private val categoryOptionCombo: CategoryOptionCombo = mock {
        on { uid() } doReturn CATEGORY_OPTION_COMBO_UID
    }
    private val event: Event = mock {
        on { attributeOptionCombo() } doReturn CATEGORY_OPTION_COMBO_UID
    }

    private lateinit var configureEventCatCombo: ConfigureEventCatCombo

    @Before
    fun setUp() {
        configureEventCatCombo = ConfigureEventCatCombo(repository = repository)
        whenever(repository.catCombo()) doReturn categoryCombo
        whenever(repository.getEvent()) doReturn event
        whenever(repository.getOptionsFromCatOptionCombo()) doReturn emptyMap()
    }

    @Test
    fun `Should be completed when Category combo is default`() = runBlocking {
        // Given a default category combo
        whenever(categoryCombo.isDefault) doReturn true
        whenever(
            repository.getCatOptionCombos(CATEGORY_COMBO_UID),
        ) doReturn listOf(categoryOptionCombo)

        // When catCombo is invoked
        val eventCatCombo = configureEventCatCombo.invoke().first()

        // Then should be completed
        assertTrue(eventCatCombo.isCompleted)
    }

    @Test
    fun `Should be completed when Category combo is not default`() = runBlocking {
        // Given a non default category combo
        whenever(categoryCombo.isDefault) doReturn false
        // And there is a category option selected by one category
        val categoryOption: CategoryOption = mock {
            on { uid() } doReturn CATEGORY_OPTION_UID
        }
        val selectedCategoryOption = Pair(CATEGORY_UID, CATEGORY_OPTION_UID)
        whenever(
            repository.getCatOption(CATEGORY_OPTION_UID),
        ) doReturn categoryOption
        whenever(
            repository.getCategoryOptionCombo(CATEGORY_COMBO_UID, listOf(CATEGORY_OPTION_UID)),
        ) doReturn CATEGORY_OPTION_COMBO_UID

        // When catCombo is invoked
        val eventCatCombo = configureEventCatCombo.invoke(selectedCategoryOption).first()

        // Then should be completed
        assertTrue(eventCatCombo.isCompleted)
        // And the EventCatCombo uid should be set
        assertEquals(eventCatCombo.uid, CATEGORY_OPTION_COMBO_UID)
    }

    @Test
    fun `Should be not completed when Category combo is not default`() = runBlocking {
        // Given a non default category combo
        whenever(categoryCombo.isDefault) doReturn false
        // And there is a category option selected by one category
        val categoryOption: CategoryOption = mock {
            on { uid() } doReturn CATEGORY_OPTION_UID
        }
        whenever(
            repository.getCatOption(CATEGORY_OPTION_UID),
        ) doReturn categoryOption

        // When catCombo is invoked
        val eventCatCombo = configureEventCatCombo.invoke().first()

        // Then should be completed
        assertFalse(eventCatCombo.isCompleted)
    }

    companion object {
        const val CATEGORY_OPTION_COMBO_UID = "categoryOptionComboUid"
        const val CATEGORY_COMBO_UID = "categoryComboUid"
        const val CATEGORY_UID = "categoryUid"
        const val CATEGORY_OPTION_UID = "categoryOptionUid"
    }
}
