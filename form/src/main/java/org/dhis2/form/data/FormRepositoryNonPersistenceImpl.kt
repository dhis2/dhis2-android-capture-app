package org.dhis2.form.data

import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult

class FormRepositoryNonPersistenceImpl : FormRepository {

    private var itemList: List<FieldUiModel> = emptyList()

    override fun processUserAction(action: RowAction): StoreResult {
        return when (action.type) {
            ActionType.ON_SAVE -> {
                updateValueOnList(action.id, action.value)
                StoreResult(
                    uid = action.id,
                    valueStoreResult = ValueStoreResult.VALUE_CHANGED
                )
            }
            ActionType.ON_TEXT_CHANGE -> {
                updateValueOnList(action.id, action.value)
                StoreResult(action.id, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
            }
            else -> {
                StoreResult(action.id, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
            }
        }
    }

    override fun composeList(list: List<FieldUiModel>?): List<FieldUiModel> {
        list?.let {
            itemList = it
        }
        return itemList
    }

    private fun updateValueOnList(uid: String, value: String?) {
        itemList.let { list ->
            list.find { item ->
                item.uid == uid
            }?.let { item ->
                itemList = list.updated(
                    list.indexOf(item),
                    item.setValue(value)
                )
            }
        }
    }
}
