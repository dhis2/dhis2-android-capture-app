package org.dhis2.form.data

import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper
import org.hisp.dhis.android.core.common.ValueType
import java.io.File
import kotlin.properties.Delegates

class FormRepositoryImpl(
    private val d2: D2
) : FormRepository {

    private val itemsWithError = mutableListOf<RowAction>()
    private var itemList: List<FieldUiModel> = emptyList()
    private var focusedItem: RowAction? = null
    var storeValue: (uid: String, value: String?, extraData: String?) -> StoreResult by Delegates.notNull()

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
                    storeValue(action.id, action.value, action.extraData)
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

                itemList.let { list ->
                    list.find { item ->
                        item.getUid() == action.id
                    }?.let { item ->
                        itemList = list.updated(
                            list.indexOf(item),
                            item.setValue(action.value)
                        )
                    }
                }
                StoreResult(action.id)
            }
        }
    }

    override fun composeList(list: List<FieldUiModel>?): List<FieldUiModel> {
        list?.let {
            itemList = it
        }
        val listWithErrors = mergeListWithErrorFields(itemList, itemsWithError)
        return setFocusedItem(listWithErrors).toMutableList()
    }

    private fun mergeListWithErrorFields(
        list: List<FieldUiModel>,
        fieldsWithError: MutableList<RowAction>
    ): List<FieldUiModel> {
        return list.map { item ->
            fieldsWithError.find { it.id == item.getUid() }?.let { action ->
                item.setValue(action.value).setError(action.error)
            } ?: item
        }
    }

    private fun setFocusedItem(list: List<FieldUiModel>) = focusedItem?.let {
        val uid = if (it.type == ActionType.ON_NEXT) {
            getNextItem(it.id)
        } else {
            it.id
        }

        list.find { item ->
            item.getUid() == uid
        }?.let { item ->
            list.updated(list.indexOf(item), item.setFocus())
        } ?: list
    } ?: list

    private fun getNextItem(currentItemUid: String): String? {
        itemList.let { fields ->
            val oldItem = fields.find { it.getUid() == currentItemUid }
            val pos = fields.indexOf(oldItem)
            if (pos < fields.size - 1) {
                return fields[pos + 1].getUid()
            }
        }

        return null
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

    internal fun saveFileResource(path: String): String {
        val file = FileResizerHelper.resizeFile(File(path), FileResizerHelper.Dimension.MEDIUM)
        return d2.fileResourceModule().fileResources().blockingAdd(file)
    }

    //TODO review validations
    internal fun withValueTypeCheck(value: String?, valueType: ValueType?) = value?.let {
        return when (valueType) {
            ValueType.PERCENTAGE,
            ValueType.INTEGER,
            ValueType.INTEGER_POSITIVE,
            ValueType.INTEGER_NEGATIVE,
            ValueType.INTEGER_ZERO_OR_POSITIVE -> (
                it.toIntOrNull() ?: it.toFloat().toInt()
                ).toString()
            ValueType.UNIT_INTERVAL -> it.toFloat().toString()
            else -> value
        }
    } ?: value

    internal fun assureCodeForOptionSet(optionSetUid: String?, value: String): String {
        return optionSetUid?.let {
            if (d2.optionModule().options()
                    .byOptionSetUid().eq(it)
                    .byName().eq(value)
                    .one().blockingExists()
            ) {
                d2.optionModule().options().byOptionSetUid().eq(it).byName().eq(value).one()
                    .blockingGet().code()
            } else {
                value
            }
        } ?: value
    }

    fun <E> Iterable<E>.updated(index: Int, elem: E): List<E> =
        mapIndexed { i, existing -> if (i == index) elem else existing }
}