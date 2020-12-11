package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import com.google.auto.value.AutoValue
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.option.Option

const val labelTag = "tag"

@AutoValue
abstract class MatrixOptionSetModel : FieldViewModel() {

    private val optionsToHide: MutableList<String> = mutableListOf()

    override fun getLayoutId(): Int {
        return R.layout.matrix_option_set
    }

    abstract fun options(): List<Option>

    abstract fun numberOfColumns(): Int

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
            focusProcessor: FlowableProcessor<HashMap<String, Boolean>>,
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
                focusProcessor,
                options,
                numberOfColumns
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
            focusProcessor(),
            options(),
            numberOfColumns()
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
            focusProcessor(),
            options(),
            numberOfColumns()
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
            focusProcessor(),
            options(),
            numberOfColumns()
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
            focusProcessor(),
            options(),
            numberOfColumns()
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
            focusProcessor(),
            options(),
            numberOfColumns()
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
        processor()?.onNext(RowAction.create(uid(), nextValue))
    }

    fun labelTag(): String = "${labelTag}_${uid()}"
    fun optionTag(option: Option): String = "${labelTag}_${option.uid()}"
    fun setOptionsToHide(
        optionsToHide: List<String>,
        optionsInGroupsToHide: List<String>,
        optionsInGroupsToShow: List<String>
    ) {
        this.optionsToHide.apply {
            clear()
            addAll(
                optionsToHide.union(optionsInGroupsToHide)
                    .filter { optionUidToHide ->
                        !optionsInGroupsToShow.contains(optionUidToHide)
                    }
            )
        }
    }

    fun optionsToShow(): List<Option> {
        return options().filter { option ->
            !optionsToHide.contains(option.uid())
        }
    }
}
