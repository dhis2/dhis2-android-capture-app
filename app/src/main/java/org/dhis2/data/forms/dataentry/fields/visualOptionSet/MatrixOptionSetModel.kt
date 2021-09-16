package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import com.google.auto.value.AutoValue
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.option.Option

const val labelTag = "tag"

@AutoValue
abstract class MatrixOptionSetModel : FieldViewModel() {

    abstract fun options(): List<Option>

    abstract fun numberOfColumns(): Int

    abstract fun optionsToHide(): List<String>

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
            numberOfColumns: Int
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
                false,
                options,
                numberOfColumns,
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
            activated(),
            options(),
            numberOfColumns(),
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
            activated(),
            options(),
            numberOfColumns(),
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
            activated(),
            options(),
            numberOfColumns(),
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
            activated(),
            options(),
            numberOfColumns(),
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
            activated(),
            options(),
            numberOfColumns(),
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
            isFocused,
            options(),
            numberOfColumns(),
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
    ): FieldViewModel {
        val options = optionsToHide.union(optionsInGroupsToHide)
            .filter { optionUidToHide ->
                !optionsInGroupsToShow.contains(optionUidToHide)
            }

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
            activated(),
            options(),
            numberOfColumns(),
            options
        )
    }

    fun optionsToShow(): List<Option> {
        return options().filter { option ->
            !optionsToHide().contains(option.uid())
        }
    }

    override fun equals(item: FieldUiModel): Boolean {
        return super.equals(item) && item is MatrixOptionSetModel &&
            this.options() == item.options() &&
            this.numberOfColumns() == item.numberOfColumns() &&
            this.optionsToHide() == item.optionsToHide()
    }
}
