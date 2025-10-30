package org.dhis2.form.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.SearchOptionSetOption
import org.hisp.dhis.android.core.option.Option
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class OptionSetDialogViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val optionSetUid = "uid"
    private val searchOptionSetOption: SearchOptionSetOption = mock()
    private val field: FieldUiModel =
        mock {
            on { optionSet } doReturn optionSetUid
        }
    private lateinit var viewModel: OptionSetDialogViewModel

    val testingDispatcher = StandardTestDispatcher()

    val dispatchers: DispatcherProvider =
        object : DispatcherProvider {
            override fun io(): CoroutineDispatcher = testingDispatcher

            override fun computation(): CoroutineDispatcher = testingDispatcher

            override fun ui(): CoroutineDispatcher = testingDispatcher
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(
            searchOptionSetOption(
                optionSetUid,
                "",
                emptyList(),
                emptyList(),
            ),
        ) doReturn mockedOptions
        viewModel =
            OptionSetDialogViewModel(
                searchOptionSetOption,
                field,
                dispatchers,
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
        verify(searchOptionSetOption, times(1))(
            optionSetUid,
            "test",
            emptyList(),
            emptyList(),
        )
    }

    private val mockedOptions =
        mutableListOf<Option>().apply {
            repeat(times = 5) { index ->
                add(
                    Option
                        .builder()
                        .uid("Option$index")
                        .displayName("name$index")
                        .code("code$index")
                        .build(),
                )
            }
        }
}
