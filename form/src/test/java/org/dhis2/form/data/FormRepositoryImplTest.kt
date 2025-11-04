package org.dhis2.form.data

import androidx.databinding.ObservableField
import io.reactivex.Flowable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.mobile.commons.providers.FieldErrorMessageProvider
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FormRepositoryImplTest {
    private val rulesUtilsProvider: RulesUtilsProvider = mock()
    private val ruleEngineHelper: RuleEngineHelper = mock()
    private val dataEntryRepository: DataEntryRepository = mock()
    private val formValueStore: FormValueStore = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val fieldErrorMessageProvider: FieldErrorMessageProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private val legendValueProvider: LegendValueProvider = mock()
    private lateinit var repository: FormRepositoryImpl

    @Before
    fun setUp(): Unit =
        runBlocking {
            whenever(dataEntryRepository.disableCollapsableSections()) doReturn null
            whenever(dataEntryRepository.firstSectionToOpen()) doReturn mockedSections().first()
            whenever(dataEntryRepository.sectionUids()) doReturn Flowable.just(mockedSections())
            whenever(dataEntryRepository.list()) doReturn Flowable.just(provideItemList())
            whenever(fieldErrorMessageProvider.mandatoryWarning()) doReturn ""
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

            repository =
                FormRepositoryImpl(
                    formValueStore,
                    fieldErrorMessageProvider,
                    displayNameProvider,
                    dataEntryRepository,
                    ruleEngineHelper,
                    rulesUtilsProvider,
                    legendValueProvider,
                    false,
                    preferenceProvider,
                )
            repository.fetchFormItems()
        }

    @Test
    fun `Should process user action ON_FOCUS`() =
        runBlocking {
            val action =
                RowAction(
                    id = "uid001",
                    type = ActionType.ON_FOCUS,
                )
            repository.setFocusedItem(action)
            assertTrue(repository.composeList().find { it.uid == "uid001" }?.focused == true)
        }

    @Test
    fun `Should process user action ON_NEXT`() =
        runBlocking {
            val action =
                RowAction(
                    id = "uid001",
                    type = ActionType.ON_NEXT,
                )
            repository.setFocusedItem(action)
            assertTrue(repository.composeList().find { it.uid == "uid002" }?.focused == true)
        }

    @Test
    fun `Should process user action ON_TEXT_CHANGE`() =
        runBlocking {
            repository.updateValueOnList("uid001", "valueChanged", ValueType.TEXT)
            assertTrue(repository.composeList().find { it.uid == "uid001" }?.value == "valueChanged")
        }

    @Test
    fun `Should process user action ON_SAVE`() =
        runBlocking {
            val action =
                RowAction(
                    id = "uid001",
                    value = "testValue",
                    type = ActionType.ON_SAVE,
                )
            whenever(formValueStore.save(action.id, action.value, null)) doReturn
                StoreResult(
                    action.id,
                    ValueStoreResult.VALUE_CHANGED,
                )
            val result = repository.save("uid001", "testValue", null)
            assertThat(result.valueStoreResult, `is`(ValueStoreResult.VALUE_CHANGED))
        }

    @Test
    fun `Should process user action ON_CLEAR`() =
        runBlocking {
            repository.removeAllValues()
            val list = repository.composeList()
            assertTrue(list.all { it.value == null && it.displayName == null })
        }

    @Test
    fun `Should update not save an item with error when ON_SAVE`() =
        runBlocking {
            // When user updates a field with error
            repository.updateErrorList(
                RowAction(
                    id = "uid001",
                    value = "testValue",
                    type = ActionType.ON_SAVE,
                    error = Throwable(),
                ),
            )

            whenever(
                fieldErrorMessageProvider.getFriendlyErrorMessage(any()),
            ) doReturn "errorMessage"

            // Then item should not be saved
            assertTrue(repository.composeList().find { it.uid == "uid001" }?.error == "errorMessage")
        }

    @Test
    fun `Should set focus to the next editable item when tapping on next`() =
        runBlocking {
            // Given a list with first item focused
            repository.composeList()
            repository.setFocusedItem(
                RowAction(
                    id = "uid001",
                    value = "value",
                    type = ActionType.ON_FOCUS,
                ),
            )

            // When user taps on next
            repository.setFocusedItem(
                RowAction(
                    id = "uid001",
                    value = "value",
                    type = ActionType.ON_NEXT,
                ),
            )

            // Then result list should has second item focused
            assertFalse(repository.composeList()[0].focused)
            assertTrue(repository.composeList()[1].focused)
        }

    @Test
    fun `Should apply program rules`(): Unit =
        runBlocking {
            whenever(ruleEngineHelper.evaluate()) doReturn
                listOf(
                    RuleEffect(
                        "",
                        RuleAction(
                            "assignedValue",
                            ProgramRuleActionType.ASSIGN.name,
                            mutableMapOf(Pair("field", "uid001")),
                        ),
                    ),
                    RuleEffect(
                        "rule2",
                        RuleAction(
                            "option1",
                            ProgramRuleActionType.HIDEOPTION.name,
                            mutableMapOf(Pair("field", "uid004")),
                        ),
                    ),
                )

            whenever(dataEntryRepository.isEvent()) doReturn true

            whenever(
                rulesUtilsProvider.applyRuleEffects(any(), any(), any(), any()),
            ) doReturn
                RuleUtilsProviderResult(
                    canComplete = true,
                    messageOnComplete = null,
                    fieldsWithErrors = emptyList(),
                    fieldsWithWarnings = emptyList(),
                    unsupportedRules = emptyList(),
                    fieldsToUpdate = listOf(FieldWithNewValue("uid001", "newValue")),
                    configurationErrors = emptyList(),
                    stagesToHide = emptyList(),
                    optionsToHide =
                        mapOf(
                            "uid004" to listOf("option1"),
                        ),
                    optionGroupsToHide = emptyMap(),
                    optionGroupsToShow = emptyMap(),
                )

            whenever(dataEntryRepository.options(any(), any(), any(), any())) doReturn
                Pair(
                    MutableStateFlow(""),
                    emptyFlow(),
                )

            repository.composeList()

            verify(rulesUtilsProvider, atLeast(1)).applyRuleEffects(
                any(),
                any(),
                any(),
                any(),
            )
        }

    @Test
    fun `Should remove sections with no fields`() =
        runBlocking {
            whenever(dataEntryRepository.list()) doReturn Flowable.just(provideEmptySectionItemList())
            whenever(
                dataEntryRepository.updateSection(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            ) doReturnConsecutively
                listOf(
                    section1().apply { totalFields = 3 },
                    section2().apply { totalFields = 0 },
                )
            val result = repository.fetchFormItems()
            assertTrue(
                result.find { it.isSection() && it.uid == "section1" } != null,
            )
            assertTrue(
                result.find { it.isSection() && it.uid == "section2" } == null,
            )
        }

    @Test
    fun `Should clear mandatory fields when composing a new list`() =
        runBlocking {
            whenever(dataEntryRepository.list()) doReturn Flowable.just(provideMandatoryItemList())
            repository.fetchFormItems()
            assertTrue(repository.runDataIntegrityCheck(false) is MissingMandatoryResult)
            whenever(
                dataEntryRepository.list(),
            ) doReturn Flowable.just(provideMandatoryItemList().filter { !it.mandatory })
            repository.fetchFormItems()
            assertTrue(repository.runDataIntegrityCheck(false) is SuccessfulResult)
        }

    @Test
    fun `Should allow to complete only uncompleted events`() =
        runBlocking {
            whenever(
                dataEntryRepository.list(),
            ) doReturn Flowable.just(provideMandatoryItemList())
            whenever(dataEntryRepository.isEvent()) doReturn true
            whenever(formValueStore.eventState()) doReturn EventStatus.ACTIVE
            repository.fetchFormItems()
            assertTrue(repository.runDataIntegrityCheck(false) is MissingMandatoryResult)
            assertTrue(repository.runDataIntegrityCheck(false).canComplete)
            whenever(formValueStore.eventState()) doReturn EventStatus.COMPLETED
            repository.fetchFormItems()

            assertTrue(repository.runDataIntegrityCheck(false) is MissingMandatoryResult)
            assertFalse(repository.runDataIntegrityCheck(false).canComplete)
        }

    @Test
    fun `Should allow discard Changes in event forms if navigating back`() =
        runBlocking {
            whenever(
                dataEntryRepository.list(),
            ) doReturn Flowable.just(provideMandatoryListWithCategoryCombo("option1"))
            repository.fetchFormItems()
            whenever(dataEntryRepository.isEvent()) doReturn true
            whenever(dataEntryRepository.validationStrategy()) doReturn ValidationStrategy.ON_COMPLETE
            whenever(formValueStore.eventState()) doReturn EventStatus.ACTIVE
            // When user updates a field with error
            repository.updateErrorList(
                RowAction(
                    id = "uid001",
                    value = "testValue",
                    type = ActionType.ON_SAVE,
                    error = Throwable(),
                ),
            )

            whenever(
                fieldErrorMessageProvider.getFriendlyErrorMessage(any()),
            ) doReturn "errorMessage"
            assertTrue(
                repository.runDataIntegrityCheck(true) is MissingMandatoryResult,
            )
            assertTrue(repository.runDataIntegrityCheck(true).allowDiscard)
        }

    @Test
    fun `Events should follow validation strategy when form has errors`() =
        runBlocking {
            whenever(
                dataEntryRepository.list(),
            ) doReturn Flowable.just(provideItemList())
            repository.fetchFormItems()

            whenever(dataEntryRepository.isEvent()) doReturn true
            whenever(dataEntryRepository.validationStrategy()) doReturn ValidationStrategy.ON_COMPLETE
            whenever(formValueStore.eventState()) doReturn EventStatus.ACTIVE
            // When user updates a field with error
            repository.updateErrorList(
                RowAction(
                    id = "uid001",
                    value = "testValue",
                    type = ActionType.ON_SAVE,
                    error = Throwable(),
                ),
            )

            whenever(
                fieldErrorMessageProvider.getFriendlyErrorMessage(any()),
            ) doReturn "errorMessage"

            assertTrue(
                repository.runDataIntegrityCheck(true) is FieldsWithErrorResult &&
                    repository.runDataIntegrityCheck(true).allowDiscard,
            )
            whenever(dataEntryRepository.validationStrategy()) doReturn ValidationStrategy.ON_UPDATE_AND_INSERT
            repository.fetchFormItems()

            assertFalse(repository.runDataIntegrityCheck(false).allowDiscard)
        }

    @Test
    fun `Should not allow to exit form with errors if event is completed`() =
        runBlocking {
            whenever(
                dataEntryRepository.list(),
            ) doReturn Flowable.just(provideMandatoryItemList())
            whenever(dataEntryRepository.isEvent()) doReturn true
            whenever(dataEntryRepository.validationStrategy()) doReturn ValidationStrategy.ON_COMPLETE
            whenever(formValueStore.eventState()) doReturn EventStatus.COMPLETED
            repository.fetchFormItems()
            assertTrue(repository.runDataIntegrityCheck(false) is MissingMandatoryResult)
            assertFalse(repository.runDataIntegrityCheck(false).allowDiscard)

            whenever(dataEntryRepository.validationStrategy()) doReturn ValidationStrategy.ON_UPDATE_AND_INSERT
            repository.fetchFormItems()
            assertTrue(repository.runDataIntegrityCheck(false) is MissingMandatoryResult)
            assertFalse(repository.runDataIntegrityCheck(false).allowDiscard)
            assertTrue(repository.runDataIntegrityCheck(true).allowDiscard)
        }

    @Test
    fun `Concurrent crash test`() =
        runBlocking {
            val ruleEffects = emptyList<RuleEffect>()
            whenever(dataEntryRepository.list()) doReturn Flowable.just(provideMandatoryItemList())
            whenever(ruleEngineHelper.evaluate()) doReturn ruleEffects
            whenever(dataEntryRepository.isEvent()) doReturn true
            whenever(
                rulesUtilsProvider.applyRuleEffects(any(), any(), any(), any()),
            ) doReturn
                RuleUtilsProviderResult(
                    canComplete = true,
                    messageOnComplete = null,
                    fieldsWithErrors = emptyList(),
                    fieldsWithWarnings = emptyList(),
                    unsupportedRules = emptyList(),
                    fieldsToUpdate = listOf(FieldWithNewValue("uid002", "newValue")),
                    configurationErrors = emptyList(),
                    stagesToHide = emptyList(),
                    optionsToHide = emptyMap(),
                    optionGroupsToHide = emptyMap(),
                    optionGroupsToShow = emptyMap(),
                )
            try {
                repository.fetchFormItems()
                assertTrue(true)
            } catch (e: Exception) {
                fail()
            }
        }

    @Test
    fun `Should show mandatory warning when some cat combo is missing`() =
        runBlocking {
            whenever(
                dataEntryRepository.list(),
            ) doReturn Flowable.just(provideMandatoryListWithCategoryCombo("option1"))
            repository.fetchFormItems()
            assertTrue(repository.runDataIntegrityCheck(false) is MissingMandatoryResult)
            whenever(
                dataEntryRepository.list(),
            ) doReturn Flowable.just(provideMandatoryListWithCategoryCombo("option1,option2"))
            repository.fetchFormItems()
            assertTrue(repository.runDataIntegrityCheck(false) is SuccessfulResult)
        }

    private fun mockedSections() =
        listOf(
            "section1",
        )

    private fun provideItemList() =
        listOf<FieldUiModel>(
            FieldUiModelImpl(
                uid = "uid001",
                value = "value",
                label = "field1",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
            FieldUiModelImpl(
                uid = "uid002",
                value = "value",
                label = "field2",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
            FieldUiModelImpl(
                uid = "uid003",
                value = "value",
                label = "field3",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
            FieldUiModelImpl(
                uid = "uid004",
                value = null,
                label = "field4",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSet = "optionSetUid",
                optionSetConfiguration =
                    OptionSetConfiguration(
                        MutableStateFlow(""),
                        {},
                        emptyFlow(),
                    ),
                autocompleteList = null,
            ),
        )

    private fun section1() =
        SectionUiModelImpl(
            uid = "section1",
            label = "section1",
            selectedField = ObservableField(""),
        )

    private fun section2() =
        SectionUiModelImpl(
            uid = "section2",
            label = "section2",
            selectedField = ObservableField(""),
        )

    private fun provideEmptySectionItemList() =
        listOf(
            section1(),
            FieldUiModelImpl(
                uid = "uid001",
                value = "value",
                displayName = "displayValue",
                label = "field1",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
            FieldUiModelImpl(
                uid = "uid002",
                value = "value",
                displayName = "displayValue",
                label = "field2",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
            FieldUiModelImpl(
                uid = "uid003",
                value = "value",
                displayName = "displayValue",
                label = "field3",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
            section2(),
        )

    private fun provideMandatoryListWithCategoryCombo(value: String) =
        listOf(
            section1(),
            FieldUiModelImpl(
                uid = "EVENT_CATEGORY_COMBO_UID-uid001",
                value = value,
                displayName = "displayValue",
                label = "field1",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                mandatory = true,
                optionSetConfiguration = null,
                autocompleteList = null,
                eventCategories =
                    listOf(
                        EventCategory("categoryUid1", "Category1", emptyList()),
                        EventCategory("categoryUid2", "Category2", emptyList()),
                    ),
            ),
        )

    private fun provideMandatoryItemList() =
        listOf(
            section1(),
            FieldUiModelImpl(
                uid = "uid001",
                value = null,
                displayName = "displayValue",
                label = "field1",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                mandatory = true,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
            FieldUiModelImpl(
                uid = "uid002",
                value = "value",
                displayName = "displayValue",
                label = "field2",
                valueType = ValueType.TEXT,
                programStageSection = "section1",
                uiEventFactory = null,
                optionSetConfiguration = null,
                autocompleteList = null,
            ),
        )

    @Test
    fun `reEvaluateRequestParams should call dataEntryRepository and map results`() {
        val customIntentUid = "custom-intent-uid"
        val evaluatedParams =
            mapOf(
                "param1" to "value1",
                "param2" to 123,
                "param3" to null,
            )

        whenever(
            dataEntryRepository.evaluateCustomIntentRequestParameters(customIntentUid),
        ) doReturn evaluatedParams

        val result = repository.reEvaluateRequestParams(customIntentUid)

        assertEquals(2, result.size)
        assertEquals("param1", result[0].key)
        assertEquals("value1", result[0].value)
        assertEquals("param2", result[1].key)
        assertEquals(123, result[1].value)
        verify(dataEntryRepository).evaluateCustomIntentRequestParameters(customIntentUid)
    }

    @Test
    fun `reEvaluateRequestParams should filter out null values`() {
        val customIntentUid = "custom-intent-uid"
        val evaluatedParams =
            mapOf(
                "param1" to null,
                "param2" to null,
            )

        whenever(
            dataEntryRepository.evaluateCustomIntentRequestParameters(customIntentUid),
        ) doReturn evaluatedParams

        val result = repository.reEvaluateRequestParams(customIntentUid)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `reEvaluateRequestParams should return empty list when no params`() {
        val customIntentUid = "custom-intent-uid"

        whenever(
            dataEntryRepository.evaluateCustomIntentRequestParameters(customIntentUid),
        ) doReturn emptyMap()

        val result = repository.reEvaluateRequestParams(customIntentUid)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `setFieldLoading should update field with loading state`() {
        val fieldUid = "field-uid"

        repository.setFieldLoading(fieldUid, true)

        verify(dataEntryRepository, atLeast(1)).updateField(
            any<FieldUiModel>(),
            anyOrNull<String>(),
            any<List<String>>(),
            any<List<String>>(),
            any<List<String>>(),
        )
    }
}
