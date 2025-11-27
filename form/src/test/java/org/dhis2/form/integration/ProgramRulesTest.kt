package org.dhis2.form.integration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.databinding.ObservableField
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.DataEntryRepository
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.FormViewModel
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.FormResultDialogProvider
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProgramRulesTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val d2: D2 = mock()
    private val optionRepository: OptionsRepository = mock()
    private val formValueStore: FormValueStore = mock()

    private val ruleEngineHelper: RuleEngineHelper = mock()
    private val rulesUtilsProvider: RulesUtilsProvider =
        RulesUtilsProviderImpl(d2, optionRepository)
    private val dataEntryRepository: DataEntryRepository = mock()

    private lateinit var repository: FormRepository

    private val preferenceProvider: PreferenceProvider = mock()
    private val geometryController: GeometryController = mock()

    private lateinit var formViewModel: FormViewModel

    private val testingDispatcher = StandardTestDispatcher()

    private val resultDialogUiProvider: FormResultDialogProvider = mock()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testingDispatcher)

        whenever(dataEntryRepository.isEvent()) doReturn true
        whenever(dataEntryRepository.disableCollapsableSections()) doReturn false
        whenever(dataEntryRepository.list()) doReturn Flowable.just(provideItemList())
        whenever(
            dataEntryRepository.updateField(
                any<FieldUiModel>(),
                anyOrNull<String>(),
                any<List<String>>(),
                any<List<String>>(),
                any<List<String>>(),
            ),
        ).thenAnswer { invocationOnMock ->
            invocationOnMock.getArgument(0) as FieldUiModel
        }

        whenever(
            dataEntryRepository.updateSection(
                any<FieldUiModel>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
            ),
        ).thenAnswer { invocationOnMock ->
            invocationOnMock.getArgument(0) as FieldUiModel
        }

        whenever(formValueStore.save(any(), anyOrNull(), anyOrNull())) doReturn
            StoreResult(
                "",
                ValueStoreResult.VALUE_CHANGED,
            )

        repository =
            FormRepositoryImpl(
                formValueStore = formValueStore,
                fieldErrorMessageProvider = mock(),
                displayNameProvider = mock(),
                dataEntryRepository = dataEntryRepository,
                ruleEngineRepository = ruleEngineHelper,
                rulesUtilsProvider = rulesUtilsProvider,
                legendValueProvider = mock(),
                useCompose = true,
                preferenceProvider = preferenceProvider,
            )

        whenever(repository.getDateFormatConfiguration()) doReturn "ddMMyyyy"

        formViewModel =
            FormViewModel(
                repository,
                object : DispatcherProvider {
                    override fun io(): CoroutineDispatcher = testingDispatcher

                    override fun computation(): CoroutineDispatcher = testingDispatcher

                    override fun ui(): CoroutineDispatcher = testingDispatcher
                },
                geometryController,
                resultDialogUiProvider = resultDialogUiProvider,
            )

        testingDispatcher.scheduler.advanceUntilIdle()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should assign a value`() =
        runTest {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "",
                        RuleAction(
                            "assignedValue",
                            ProgramRuleActionType.ASSIGN.name,
                            mutableMapOf(Pair("field", "uid001")),
                        ),
                        "newValue",
                    ),
                )

            val intent =
                FormIntent.OnSave(
                    uid = "uid004",
                    value = "newValue04",
                    valueType = ValueType.TEXT,
                )

            formViewModel.submitIntent(intent)
            advanceUntilIdle()

            val items = formViewModel.items.value ?: emptyList()

            assert(items.find { it.uid == "uid001" }?.value == "newValue")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should hide field`() =
        runTest {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "ruleUid",
                        RuleAction(
                            "data",
                            ProgramRuleActionType.HIDEFIELD.name,
                            mutableMapOf(
                                "content" to "content",
                                "field" to "uid001",
                            ),
                        ),
                    ),
                )

            val intent =
                FormIntent.OnSave(
                    uid = "uid004",
                    value = "newValue04",
                    valueType = ValueType.TEXT,
                )

            formViewModel.submitIntent(intent)
            advanceUntilIdle()

            val items = formViewModel.items.value ?: emptyList()

            items.forEach {
                assert(it.uid != "uid001")
            }
            assert(items.size == 6)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should hide section`() =
        runTest {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "ruleUid",
                        RuleAction(
                            "data",
                            ProgramRuleActionType.HIDESECTION.name,
                            mutableMapOf(
                                "content" to "content",
                                "programStageSection" to "section1",
                            ),
                        ),
                        "data",
                    ),
                )

            val intent =
                FormIntent.OnSave(
                    uid = "uid004",
                    value = "newValue04",
                    valueType = ValueType.TEXT,
                )

            formViewModel.submitIntent(intent)
            advanceUntilIdle()

            val items = formViewModel.items.value ?: emptyList()

            assertTrue(items.size == 4)
            assertTrue(items[0].uid == "uid004")
            assertTrue(items[1].uid == "uid005")
            assertTrue(items[2].uid == "uid006")
            assertTrue(items[3].uid == "uid007")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should show warning and error message`() =
        runTest {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "ruleUid",
                        RuleAction(
                            "data",
                            ProgramRuleActionType.SHOWWARNING.name,
                            mutableMapOf(
                                "content" to "content",
                                "field" to "uid002",
                            ),
                        ),
                        "warning message",
                    ),
                    RuleEffect(
                        "ruleUid2",
                        RuleAction(
                            "data",
                            ProgramRuleActionType.SHOWERROR.name,
                            mutableMapOf(
                                "content" to "content",
                                "field" to "uid005",
                            ),
                        ),
                        "error message",
                    ),
                )

            val intent =
                FormIntent.OnSave(
                    uid = "uid004",
                    value = "value04",
                    valueType = ValueType.TEXT,
                )

            formViewModel.submitIntent(intent)
            advanceUntilIdle()

            val items = formViewModel.items.value ?: emptyList()

            items.forEach {
                if (it.uid == "uid002") {
                    assertNotNull(it.warning)
                    assertEquals(it.warning, "content warning message")
                }
                if (it.uid == "uid005") {
                    assertNotNull(it.error)
                    assertEquals(it.error, "content error message")
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should set mandatory field`() =
        runTest {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "ruleUid",
                        RuleAction(
                            "data",
                            ProgramRuleActionType.SETMANDATORYFIELD.name,
                            mutableMapOf(
                                "content" to "content",
                                "field" to "uid003",
                            ),
                        ),
                        "data",
                    ),
                )

            val intent =
                FormIntent.OnSave(
                    uid = "uid004",
                    value = "value04",
                    valueType = ValueType.TEXT,
                )

            formViewModel.submitIntent(intent)
            advanceUntilIdle()

            val items = formViewModel.items.value ?: emptyList()

            items.forEach {
                if (it.uid == "uid003") {
                    assertTrue(it.mandatory)
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should show option`() =
        runTest {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "ruleUid",
                        RuleAction(
                            "data",
                            ProgramRuleActionType.SHOWOPTIONGROUP.name,
                            mutableMapOf(
                                "content" to "content",
                                "field" to "uid006",
                                "optionGroup" to "optionGroupId",
                            ),
                        ),
                        "data",
                    ),
                )

            whenever(
                dataEntryRepository.options(
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            ) doReturn Pair(MutableStateFlow(""), emptyFlow())

            val intent =
                FormIntent.OnSave(
                    uid = "uid004",
                    value = "value04",
                    valueType = ValueType.TEXT,
                )

            whenever(formValueStore.deleteOptionValueIfSelected(any(), any())) doReturn
                StoreResult(
                    "uid006",
                    ValueStoreResult.VALUE_CHANGED,
                )

            formViewModel.submitIntent(intent)
            advanceUntilIdle()

            verify(dataEntryRepository).options(
                optionSetUid = "optionSetUid",
                optionsToHide = emptyList(),
                optionGroupsToHide = emptyList(),
                optionGroupsToShow = listOf("optionGroupId"),
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should hide option`() =
        runTest {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "ruleUid",
                        RuleAction(
                            "data",
                            ProgramRuleActionType.HIDEOPTION.name,
                            mutableMapOf(
                                "content" to "content",
                                "field" to "uid007",
                                "option" to "Option2",
                            ),
                        ),
                        "data",
                    ),
                )

            whenever(
                dataEntryRepository.options(
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            ) doReturn Pair(MutableStateFlow(""), emptyFlow())

            val intent =
                FormIntent.OnSave(
                    uid = "uid004",
                    value = "value04",
                    valueType = ValueType.TEXT,
                )

            whenever(formValueStore.deleteOptionValueIfSelected(any(), any())) doReturn
                StoreResult(
                    "uid007",
                    ValueStoreResult.VALUE_HAS_NOT_CHANGED,
                )

            formViewModel.submitIntent(intent)
            advanceUntilIdle()

            verify(dataEntryRepository).options(
                optionSetUid = "optionSetUid",
                optionsToHide = listOf("Option2"),
                optionGroupsToHide = emptyList(),
                optionGroupsToShow = emptyList(),
            )
        }

    private fun provideItemList() =
        listOf(
            SectionUiModelImpl(
                uid = "section1",
                label = "section1",
                selectedField = ObservableField(""),
            ),
            FieldUiModelImpl(
                uid = "uid001",
                value = "value01",
                label = "field1",
                valueType = ValueType.TEXT,
                optionSetConfiguration = null,
                autocompleteList = null,
                programStageSection = "section1",
            ),
            FieldUiModelImpl(
                uid = "uid002",
                value = "value02",
                label = "field2",
                valueType = ValueType.TEXT,
                optionSetConfiguration = null,
                autocompleteList = null,
                programStageSection = "section1",
            ),
            FieldUiModelImpl(
                uid = "uid003",
                value = "value03",
                label = "field3",
                valueType = ValueType.TEXT,
                optionSetConfiguration = null,
                autocompleteList = null,
                programStageSection = "section1",
            ),
            SectionUiModelImpl(
                uid = "section2",
                label = "section2",
                selectedField = ObservableField(""),
            ),
            FieldUiModelImpl(
                uid = "uid004",
                value = "value04",
                label = "field4",
                valueType = ValueType.TEXT,
                optionSetConfiguration = null,
                autocompleteList = null,
                programStageSection = "section2",
            ),
            FieldUiModelImpl(
                uid = "uid005",
                value = "value05",
                label = "field5",
                valueType = ValueType.TEXT,
                optionSetConfiguration = null,
                autocompleteList = null,
                programStageSection = "section2",
            ),
            FieldUiModelImpl(
                uid = "uid006",
                value = "value06",
                label = "field6",
                valueType = ValueType.MULTI_TEXT,
                optionSetConfiguration =
                    OptionSetConfiguration(
                        MutableStateFlow(""),
                        {},
                        emptyFlow(),
                    ),
                autocompleteList = null,
                programStageSection = "section2",
                optionSet = "optionSetUid",
            ),
            FieldUiModelImpl(
                uid = "uid007",
                value = "value07",
                label = "field7",
                valueType = ValueType.MULTI_TEXT,
                optionSetConfiguration =
                    OptionSetConfiguration(
                        MutableStateFlow(""),
                        {},
                        emptyFlow(),
                    ),
                autocompleteList = null,
                programStageSection = "section2",
                optionSet = "optionSetUid",
            ),
        )
}
