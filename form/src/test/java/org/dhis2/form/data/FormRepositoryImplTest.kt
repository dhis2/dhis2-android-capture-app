package org.dhis2.form.data

import androidx.databinding.ObservableField
import io.reactivex.Flowable
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FormRepositoryImplTest {

    private val rulesUtilsProvider: RulesUtilsProvider = mock()
    private val ruleEngineRepository: RuleEngineRepository = mock()
    private val dataEntryRepository: DataEntryRepository = mock()
    private val formValueStore: FormValueStore = mock()
    private val fieldErrorMessageProvider: FieldErrorMessageProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private val legendValueProvider: LegendValueProvider = mock()
    private lateinit var repository: FormRepositoryImpl

    @Before
    fun setUp() {
        whenever(dataEntryRepository.disableCollapsableSections()) doReturn null
        whenever(dataEntryRepository.sectionUids()) doReturn Flowable.just(mockedSections())
        whenever(dataEntryRepository.list()) doReturn Flowable.just(provideItemList())
        repository = FormRepositoryImpl(
            formValueStore,
            fieldErrorMessageProvider,
            displayNameProvider,
            dataEntryRepository,
            ruleEngineRepository,
            rulesUtilsProvider,
            legendValueProvider,
            false,
        )
        repository.fetchFormItems()
    }

    @Test
    fun `Should process user action ON_FOCUS`() {
        val action = RowAction(
            id = "uid001",
            type = ActionType.ON_FOCUS,
        )
        repository.setFocusedItem(action)
        assertTrue(repository.composeList().find { it.uid == "uid001" }?.focused == true)
    }

    @Test
    fun `Should process user action ON_NEXT`() {
        val action = RowAction(
            id = "uid001",
            type = ActionType.ON_NEXT,
        )
        repository.setFocusedItem(action)
        assertTrue(repository.composeList().find { it.uid == "uid002" }?.focused == true)
    }

    @Test
    fun `Should process user action ON_TEXT_CHANGE`() {
        repository.updateValueOnList("uid001", "valueChanged", ValueType.TEXT)
        assertTrue(repository.composeList().find { it.uid == "uid001" }?.value == "valueChanged")
    }

    @Test
    fun `Should process user action ON_SAVE`() {
        val action = RowAction(
            id = "uid001",
            value = "testValue",
            type = ActionType.ON_SAVE,
        )
        whenever(formValueStore.save(action.id, action.value, null)) doReturn StoreResult(
            action.id,
            ValueStoreResult.VALUE_CHANGED,
        )
        val result = repository.save("uid001", "testValue", null)
        assertThat(result?.valueStoreResult, `is`(ValueStoreResult.VALUE_CHANGED))
    }

    @Test
    fun `Should process user action ON_CLEAR`() {
        repository.removeAllValues()
        val list = repository.composeList()
        assertTrue(list.all { it.value == null && it.displayName == null })
    }

    @Test
    fun `Should update not save an item with error when ON_SAVE`() {
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
    fun `Should set focus to the next editable item when tapping on next`() {
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
    fun `Should apply program rules`() {
        whenever(ruleEngineRepository.calculate()) doReturn listOf(
            RuleEffect.create(
                "",
                RuleActionAssign.create(
                    null,
                    "assignedValue",
                    "uid001",
                ),
            ),
        )

        whenever(dataEntryRepository.isEvent()) doReturn true

        whenever(
            rulesUtilsProvider.applyRuleEffects(any(), any(), any(), any()),
        ) doReturn RuleUtilsProviderResult(
            canComplete = true,
            messageOnComplete = null,
            fieldsWithErrors = emptyList(),
            fieldsWithWarnings = emptyList(),
            unsupportedRules = emptyList(),
            fieldsToUpdate = listOf(FieldWithNewValue("uid001", "newValue")),
            configurationErrors = emptyList(),
            stagesToHide = emptyList(),
            optionsToHide = emptyMap(),
            optionGroupsToHide = emptyMap(),
            optionGroupsToShow = emptyMap(),
        )

        verify(rulesUtilsProvider, times(1)).applyRuleEffects(
            any(),
            any(),
            any(),
            any(),
        )
    }

    @Test
    fun `Should remove sections with no fields`() {
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
        ) doReturnConsecutively listOf(
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
    fun `Should clear mandatory fields when composing a new list`() {
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
    fun `Concurrent crash test`() {
        val ruleEffects = emptyList<RuleEffect>()
        whenever(dataEntryRepository.list()) doReturn Flowable.just(provideMandatoryItemList())
        whenever(ruleEngineRepository.calculate()) doReturn ruleEffects
        whenever(dataEntryRepository.isEvent()) doReturn true
        whenever(
            rulesUtilsProvider.applyRuleEffects(any(), any(), any(), any()),
        ) doReturn RuleUtilsProviderResult(
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

    private fun mockedSections() = listOf(
        "section1",
    )

    private fun provideItemList() = listOf<FieldUiModel>(
        FieldUiModelImpl(
            uid = "uid001",
            layoutId = 1,
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
            layoutId = 2,
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
            layoutId = 3,
            value = "value",
            label = "field3",
            valueType = ValueType.TEXT,
            programStageSection = "section1",
            uiEventFactory = null,
            optionSetConfiguration = null,
            autocompleteList = null,
        ),
    )

    private fun section1() = SectionUiModelImpl(
        uid = "section1",
        layoutId = 1,
        label = "section1",
        selectedField = ObservableField(""),
    )

    private fun section2() = SectionUiModelImpl(
        uid = "section2",
        layoutId = 1,
        label = "section2",
        selectedField = ObservableField(""),
    )

    private fun provideEmptySectionItemList() = listOf<FieldUiModel>(
        section1(),
        FieldUiModelImpl(
            uid = "uid001",
            layoutId = 1,
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
            layoutId = 2,
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
            layoutId = 3,
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

    private fun provideMandatoryItemList() = listOf(
        section1(),
        FieldUiModelImpl(
            uid = "uid001",
            layoutId = 1,
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
            layoutId = 2,
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
}
