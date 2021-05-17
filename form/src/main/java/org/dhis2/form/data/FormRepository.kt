package org.dhis2.form.data

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult

interface FormRepository {

    fun processUserAction(action: RowAction): StoreResult

    fun composeList(list: List<FieldUiModel>? = null): List<FieldUiModel>
}
