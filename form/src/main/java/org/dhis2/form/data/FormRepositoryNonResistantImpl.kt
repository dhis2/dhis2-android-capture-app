package org.dhis2.form.data

import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult

class FormRepositoryNonResistantImpl : FormRepository {
    override fun processUserAction(action: RowAction): StoreResult {
        return when (action.type) {
            ActionType.ON_TEXT_CHANGE,
            ActionType.ON_SAVE -> StoreResult(
                uid = action.id,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED
            )
            else -> StoreResult(action.id)
        }
    }

    override fun composeList(list: List<FieldUiModel>?): List<FieldUiModel> {
        return emptyList()
    }
}