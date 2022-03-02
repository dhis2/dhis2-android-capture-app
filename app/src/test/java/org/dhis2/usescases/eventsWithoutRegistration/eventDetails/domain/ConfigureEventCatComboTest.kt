package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.event.Event
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConfigureEventCatComboTest {

    private val eventInitialRepository: EventInitialRepository = mock()
    private val category: Category = mock {
        on { uid() } doReturn CATEGORY_UID
    }
    private val categoryCombo: CategoryCombo = mock {
        on { uid() } doReturn CATEGORY_OPTION_COMBO_UID
        on { categories() } doReturn listOf(category)
    }
    private val event: Event = mock {
        on { attributeOptionCombo() } doReturn CATEGORY_OPTION_COMBO_UID
    }

    private lateinit var configureEventCatCombo: ConfigureEventCatCombo

    @Before
    fun setUp() {
        configureEventCatCombo = ConfigureEventCatCombo(
            eventInitialRepository = eventInitialRepository,
            programUid = PROGRAM_UID,
            eventUid = EVENT_UID
        )
        whenever(
            eventInitialRepository.catCombo(PROGRAM_UID)
        ) doReturn Observable.just(categoryCombo)
        whenever(
            eventInitialRepository.event(EVENT_UID)
        ) doReturn Observable.just(event)
        whenever(
            eventInitialRepository.getOptionsFromCatOptionCombo(EVENT_UID)
        ) doReturn Flowable.just(emptyMap())
    }

    @Test
    fun `Should be completed when Category combo is default`() = runBlocking {
        // Given a default category combo
        whenever(categoryCombo.isDefault) doReturn true

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
            eventInitialRepository.getCatOption(CATEGORY_OPTION_UID)
        ) doReturn categoryOption

        // When catCombo is invoked
        val eventCatCombo = configureEventCatCombo.invoke(selectedCategoryOption).first()

        // Then should be completed
        assertTrue(eventCatCombo.isCompleted)
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
            eventInitialRepository.getCatOption(CATEGORY_OPTION_UID)
        ) doReturn categoryOption

        // When catCombo is invoked
        val eventCatCombo = configureEventCatCombo.invoke().first()

        // Then should be completed
        assertFalse(eventCatCombo.isCompleted)
    }

    companion object {
        const val PROGRAM_UID = "programUid"
        const val EVENT_UID = "eventUid"
        const val CATEGORY_OPTION_COMBO_UID = "categoryOptionComboUid"
        const val CATEGORY_UID = "categoryUid"
        const val CATEGORY_OPTION_UID = "categoryOptionUid"
    }
}
