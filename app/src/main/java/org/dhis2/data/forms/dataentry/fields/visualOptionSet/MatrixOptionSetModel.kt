package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import com.google.auto.value.AutoValue
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.option.Option

const val labelTag = "tag"

@AutoValue
abstract class MatrixOptionSetModel : FieldViewModel() {

    override val layoutId: Int
        get() = R.layout.matrix_option_set

    abstract fun options(): List<Option>

    abstract fun numberOfColumns(): Int

    abstract fun optionsToHide(): List<String>

    companion object {
        @JvmStatic
        fun create(
            fieldUid: String,
            fieldLabel: String,
            mandatory: Boolean,
            value: String?,
            stageSectionUid: String?,
            editable: Boolean?,
            optionSetUid: String?,
            description: String?,
            style: ObjectStyle,
            processor: FlowableProcessor<RowAction>?,
            url: String?,
            options: List<Option>,
            numberOfColumns: Int
        ): MatrixOptionSetModel {
            return AutoValue_MatrixOptionSetModel(
                fieldUid,
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
                processor,
                null,
                false,
                url,
                options,
                numberOfColumns,
                emptyList()
            )
        }
    }

    override fun setMandatory(): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>()
        )
    }

    override fun withError(error: String?): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>()
        )
    }

    override fun withWarning(warning: String?): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>()
        )
    }

    override fun withValue(data: String?): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>()
        )
    }

    override fun withEditMode(isEditable: Boolean): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            url(),
            options(),
            numberOfColumns(),
            emptyList<String>()
        )
    }

    override fun withFocus(isFocused: Boolean): FieldViewModel {
        return AutoValue_MatrixOptionSetModel(
            uid(),
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
            processor(),
            style(),
            isFocused,
            url(),
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
        processor()?.onNext(
            RowAction(
                id = uid(),
                value = nextValue,
                type = ActionType.ON_SAVE
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
            processor(),
            style(),
            activated(),
            url(),
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
