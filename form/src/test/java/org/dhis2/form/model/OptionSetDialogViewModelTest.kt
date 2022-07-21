package org.dhis2.form.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.form.data.OptionSetDialogRepository
import org.hisp.dhis.android.core.option.Option
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class OptionSetDialogViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val optionSetUid = "uid"
    private val repository: OptionSetDialogRepository = mock()
    private val field: FieldUiModel = mock {
        on { optionSet } doReturn optionSetUid
    }
    private lateinit var viewModel: OptionSetDialogViewModel

    val testingDispatcher = StandardTestDispatcher()

    val dispatchers: DispatcherProvider = object : DispatcherProvider {
        override fun io(): CoroutineDispatcher {
            return testingDispatcher
        }

        override fun computation(): CoroutineDispatcher {
            return testingDispatcher
        }

        override fun ui(): CoroutineDispatcher {
            return testingDispatcher
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(
            repository.searchForOption(
                optionSetUid,
                "",
                emptyList(),
                emptyList()
            )
        ) doReturn mockedOptions
        viewModel = OptionSetDialogViewModel(
            repository,
            field,
            dispatchers
        )
        testingDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should load initial options`() {
        assertTrue(viewModel.options.value?.size == 5)
    }

    @Test
    fun `Should search for options`() {
        viewModel.onSearchingOption("test")
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.searchValue.value == "test")
        verify(repository, times(1)).searchForOption(
            optionSetUid,
            "test",
            emptyList(),
            emptyList()
        )
    }

    @Test
    fun `Should search and filter options to hide`() {
        val optionsToHide = listOf("Option1")
        whenever(field.optionsToHide) doReturn optionsToHide
        viewModel.onSearchingOption("test")
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.searchValue.value == "test")
        verify(repository, times(1)).searchForOption(
            optionSetUid,
            "test",
            emptyList(),
            optionsToHide
        )
    }

    @Test
    fun `Should search and filter options to show`() {
        val optionsToShow = listOf("Option1")
        whenever(field.optionsToShow) doReturn optionsToShow
        viewModel.onSearchingOption("test")
        assertTrue(viewModel.searchValue.value == "test")
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(repository, times(1)).searchForOption(
            optionSetUid,
            "test",
            optionsToShow,
            emptyList()
        )
    }

    @Test
    fun `Should search and filter options to show and hide`() {
        val optionsToShow = listOf("Option1")
        val optionsToHide = listOf("Option1")

        whenever(field.optionsToShow) doReturn optionsToShow
        whenever(field.optionsToHide) doReturn optionsToHide
        viewModel.onSearchingOption("test")
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.searchValue.value == "test")
        verify(repository, times(1)).searchForOption(
            optionSetUid,
            "test",
            optionsToShow,
            optionsToHide
        )
    }

    private val mockedOptions = mutableListOf<Option>().apply {
        repeat(times = 5) { index ->
            add(
                Option.builder()
                    .uid("Option$index")
                    .displayName("name$index")
                    .code("code$index")
                    .build()
            )
        }
    }
}
