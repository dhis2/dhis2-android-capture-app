package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import com.google.auto.value.AutoValue
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option

const val labelTag = "tag"

@AutoValue
abstract class MatrixOptionSetModel : FieldViewModel() {

    abstract fun options(): List<Option>

    abstract fun numberOfColumns(): Int

    abstract fun optionsToHide(): List<String>

    abstract fun optionsInGroupsToShow(): List<String>

    companion object {
        @JvmStatic
        fun create(
            fieldUid: String,
            layoutId: Int,
            fieldLabel: String,
            mandatory: Boolean,
            value: String?,
            stageSectionUid: String?,
            editable: Boolean?,
            optionSetUid: String?,
            description: String?,
            style: ObjectStyle,
            options: List<Option>,
            numberOfColumns: Int,
            valueType: ValueType,
            url: String?,
        ): MatrixOptionSetModel {
            return AutoValue_MatrixOptionSetModel(
                fieldUid,
                layoutId,
                fieldLabel,
                mandatory,
                value,
                stageSectionUid,
                false,
                editable,
                optionSetUid,
                null,
                null,
                description,
                style,
                null,
                DataEntryViewHolderTypes.PICTURE,
                null,
                null,
                false,
                valueType,
                url,
                options,
                numberOfColumns,
                emptyList(),
                emptyList()
            )
        }
    }

    override fun setMandatory(): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
            layoutId(),
            label(),
            true,
            value(),
            programStageSection(),
            false,
            editable(),
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            fieldMask(),
            dataEntryViewType(),
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>(),
            emptyList<String>()
        )
    }

    override fun withError(error: String?): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            false,
            editable(),
            optionSet(),
            warning(),
            error,
            description(),
            objectStyle(),
            fieldMask(),
            dataEntryViewType(),
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>(),
            emptyList<String>()
        )
    }

    override fun withWarning(warning: String?): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            false,
            editable(),
            optionSet(),
            warning,
            error(),
            description(),
            objectStyle(),
            fieldMask(),
            dataEntryViewType(),
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>(),
            emptyList<String>()
        )
    }

    override fun withValue(data: String?): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            data,
            programStageSection(),
            false,
            editable(),
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            fieldMask(),
            dataEntryViewType(),
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>(),
            emptyList<String>()
        )
    }

    override fun withEditMode(isEditable: Boolean): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            false,
            isEditable,
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            fieldMask(),
            dataEntryViewType(),
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>(),
            emptyList<String>()
        )
    }

    override fun withFocus(isFocused: Boolean): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            false,
            editable(),
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            fieldMask(),
            dataEntryViewType(),
            style(),
            hint(),
            isFocused,
            valueType(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>(),
            emptyList<String>()
        )
    }

    fun isSelected(option: Option): Boolean {
        return value() == option.displayName()
    }

    fun selectOption(selectedOption: Option) {
        val nextValue = if (value() == selectedOption.displayName()) {
            null
        } else {
            selectedOption.code()
        }

        callback.intent(
            FormIntent.OnSave(
                uid(),
                nextValue,
                null,
                fieldMask()
            )
        )
    }

    fun labelTag(): String = "${labelTag}_${uid()}"
    fun optionTag(option: Option): String = "${labelTag}_${option.uid()}"

    fun setOptionsToHide(
        optionsToHide: List<String>,
        optionsInGroupsToHide: List<String>,
        optionsInGroupsToShow: List<String>
    ): MatrixOptionSetModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            false,
            editable(),
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            fieldMask(),
            dataEntryViewType(),
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            options(),
            numberOfColumns(),
            optionsToHide.union(optionsInGroupsToHide).toMutableList(),
            optionsInGroupsToShow
        )
    }

    fun optionsToShow(): List<Option> {
        return options().filter { option ->
            if (optionsInGroupsToShow().isEmpty()) {
                !optionsToHide().contains(option.uid())
            } else {
                optionsInGroupsToShow().contains(option.uid())
            }
        }
    }

    override fun equals(item: FieldUiModel): Boolean {
        return super.equals(item) && item is MatrixOptionSetModel &&
            this.options() == item.options() &&
            this.numberOfColumns() == item.numberOfColumns() &&
            this.optionsToHide() == item.optionsToHide()
    }
}
