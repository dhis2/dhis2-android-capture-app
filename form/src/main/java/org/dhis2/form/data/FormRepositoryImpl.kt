package org.dhis2.form.data

import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.hisp.dhis.android.core.common.ValueType

class FormRepositoryImpl(
    private val formValueStore: FormValueStore?,
    private val fieldErrorMessageProvider: FieldErrorMessageProvider,
    private val displayNameProvider: DisplayNameProvider
) : FormRepository {

    private val itemsWithError: MutableList<RowAction> = mutableListOf()
    private var itemList: List<FieldUiModel> = emptyList()
    private var focusedItem: RowAction? = null

    override fun processUserAction(action: RowAction): StoreResult {
        return when (action.type) {
            ActionType.ON_SAVE -> {
                updateErrorList(action)
                if (action.error != null) {
                    StoreResult(
                        action.id,
                        ValueStoreResult.VALUE_HAS_NOT_CHANGED
                    )
                } else {
                    if (formValueStore != null) {
                        formValueStore.save(action.id, action.value, action.extraData)
                    } else {
                        updateValueOnList(action.id, action.value, action.valueType)
                        StoreResult(
                            action.id,
                            ValueStoreResult.VALUE_CHANGED
                        )
                    }
                }
            }
            ActionType.ON_FOCUS, ActionType.ON_NEXT -> {
                this.focusedItem = action

                StoreResult(
                    action.id,
                    ValueStoreResult.VALUE_HAS_NOT_CHANGED
                )
            }

            ActionType.ON_TEXT_CHANGE -> {
                updateErrorList(action)
                updateValueOnList(action.id, action.value, action.valueType)
                StoreResult(
                    action.id,
                    ValueStoreResult.TEXT_CHANGING
                )
            }
        }
    }

    override fun composeList(list: List<FieldUiModel>?): List<FieldUiModel> {
        list?.let {
            itemList = it
        }
        val listWithErrors = mergeListWithErrorFields(itemList, itemsWithError)
        return setFocusedItem(listWithErrors)
    }

    private fun setFocusedItem(list: List<FieldUiModel>) = focusedItem?.let {
        val uid = if (it.type == ActionType.ON_NEXT) {
            getNextItem(it.id)
        } else {
            it.id
        }

        list.find { item ->
            item.uid == uid
        }?.let { item ->
            list.updated(list.indexOf(item), item.setFocus())
        } ?: list
    } ?: list

    private fun getNextItem(currentItemUid: String): String? {
        itemList.let { fields ->
            val oldItem = fields.find { it.uid == currentItemUid }
            val pos = fields.indexOf(oldItem)
            if (pos < fields.size - 1) {
                return fields[pos + 1].uid
            }
        }
        return null
    }

    private fun updateValueOnList(uid: String, value: String?, valueType: ValueType?) {
        itemList.let { list ->
            list.find { item ->
                item.uid == uid
            }?.let { item ->
                itemList = list.updated(
                    list.indexOf(item),
                    item.setValue(value)
                        .setDisplayName(displayNameProvider.provideDisplayName(valueType, value))
                )
            }
        }
    }

    private fun mergeListWithErrorFields(
        list: List<FieldUiModel>,
        fieldsWithError: MutableList<RowAction>
    ): List<FieldUiModel> {
        return list.map { item ->
            fieldsWithError.find { it.id == item.uid }?.let { action ->
                val error = action.error?.let {
                    fieldErrorMessageProvider.getFriendlyErrorMessage(it)
                }
                item.setValue(action.value).setError(error)
                    .setDisplayName(
                        displayNameProvider.provideDisplayName(
                            action.valueType,
                            action.value
                        )
                    )
            } ?: item
        }
    }

    private fun updateErrorList(action: RowAction) {
        if (action.error != null) {
            if (itemsWithError.find { it.id == action.id } == null) {
                itemsWithError.add(action)
            }
        } else {
            itemsWithError.find { it.id == action.id }?.let {
                itemsWithError.remove(it)
            }
        }
    }

    fun <E> Iterable<E>.updated(index: Int, elem: E): List<E> =
        mapIndexed { i, existing -> if (i == index) elem else existing }
}
