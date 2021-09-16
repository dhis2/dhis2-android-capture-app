package org.dhis2.data.forms.dataentry.fields.datetime

import com.google.auto.value.AutoValue
import io.reactivex.processors.FlowableProcessor
import org.dhis2.Bindings.toDate
import org.dhis2.Bindings.toTime
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.style.FormUiModelStyle
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType

@AutoValue
abstract class DateTimeViewModel : FieldViewModel() {
    abstract val isBackgroundTransparent: Boolean
    abstract fun valueType(): ValueType
    abstract val isSearchMode: Boolean
    override fun setMandatory(): FieldViewModel {
        return AutoValue_DateTimeViewModel(
            uid(),
            layoutId(),
            label(),
            true,
            value(),
            programStageSection(),
            allowFutureDate(),
            editable(),
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            null,
            provideDataEntryViewHolderType(valueType()),
            processor(),
            style(),
            activated(),
            isBackgroundTransparent,
            valueType(),
            isSearchMode
        )
    }

    override fun withError(error: String?): FieldViewModel {
        return AutoValue_DateTimeViewModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            allowFutureDate(),
            editable(),
            optionSet(),
            warning(),
            error,
            description(),
            objectStyle(),
            null,
            provideDataEntryViewHolderType(valueType()),
            processor(),
            style(),
            activated(),
            isBackgroundTransparent,
            valueType(),
            isSearchMode
        )
    }

    override fun withWarning(warning: String): FieldViewModel {
        return AutoValue_DateTimeViewModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            allowFutureDate(),
            editable(),
            optionSet(),
            warning,
            error(),
            description(),
            objectStyle(),
            null,
            provideDataEntryViewHolderType(valueType()),
            processor(),
            style(),
            activated(),
            isBackgroundTransparent,
            valueType(),
            isSearchMode
        )
    }

    override fun withValue(data: String?): FieldViewModel {
        return AutoValue_DateTimeViewModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            data,
            programStageSection(),
            allowFutureDate(),
            editable(),
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            null,
            provideDataEntryViewHolderType(valueType()),
            processor(),
            style(),
            activated(),
            isBackgroundTransparent,
            valueType(),
            isSearchMode
        )
    }

    override fun withEditMode(isEditable: Boolean): FieldViewModel {
        return AutoValue_DateTimeViewModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            allowFutureDate(),
            isEditable,
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            null,
            provideDataEntryViewHolderType(valueType()),
            processor(),
            style(),
            activated(),
            isBackgroundTransparent,
            valueType(),
            isSearchMode
        )
    }

    override fun withFocus(isFocused: Boolean): FieldViewModel {
        return AutoValue_DateTimeViewModel(
            uid(),
            layoutId(),
            label(),
            mandatory(),
            value(),
            programStageSection(),
            allowFutureDate(),
            editable(),
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            null,
            provideDataEntryViewHolderType(valueType()),
            processor(),
            style(),
            isFocused,
            isBackgroundTransparent,
            valueType(),
            isSearchMode
        )
    }

    override val layoutId: Int
        get() = R.layout.form_date_time

    val inputHint: Int
        get() = when (valueType()) {
            ValueType.TIME -> R.string.select_time
            else -> R.string.choose_date
        }

    val descIcon: Int
        get() = when (valueType()) {
            ValueType.DATE -> R.drawable.ic_form_date
            ValueType.TIME -> R.drawable.ic_form_time
            ValueType.DATETIME -> R.drawable.ic_form_date_time
            else -> R.drawable.ic_form_date_time
        }

    override fun onDescriptionClick() {
        callback.recyclerViewUiEvents(
            RecyclerViewUiEvents.ShowDescriptionLabelDialog(label(), description())
        )
    }

    fun onShowCustomCalendar() {
        onItemClick()
        val event = when (valueType()) {
            ValueType.DATE -> RecyclerViewUiEvents.OpenCustomCalendar(
                uid(),
                label(),
                value()?.toDate(),
                allowFutureDate() ?: true
            )
            ValueType.DATETIME -> RecyclerViewUiEvents.OpenCustomCalendar(
                uid(),
                label(),
                value()?.toDate(),
                allowFutureDate() ?: true,
                isDateTime = true
            )
            ValueType.TIME -> RecyclerViewUiEvents.OpenTimePicker(
                uid(),
                label(),
                value()?.toTime()
            )
            else -> null
        }
        event?.let { callback.recyclerViewUiEvents(it) }
    }

    fun onClearDateClick() {
        onItemClick()
        callback.intent(FormIntent.ClearValue(uid()))
    }

    companion object {
        @JvmStatic
        fun create(
            id: String?,
            layoutId: Int,
            label: String?,
            mandatory: Boolean?,
            type: ValueType,
            value: String?,
            section: String?,
            allowFutureDates: Boolean?,
            editable: Boolean?,
            description: String?,
            objectStyle: ObjectStyle?,
            isBackgroundTransparent: Boolean,
            isSearchMode: Boolean,
            processor: FlowableProcessor<RowAction>?,
            style: FormUiModelStyle?
        ): FieldViewModel {
            return AutoValue_DateTimeViewModel(
                id,
                layoutId,
                label,
                mandatory,
                value,
                section,
                allowFutureDates,
                editable,
                null,
                null,
                null,
                description,
                objectStyle,
                null,
                provideDataEntryViewHolderType(type),
                processor,
                style,
                false,
                isBackgroundTransparent,
                type,
                isSearchMode
            )
        }

        private fun provideDataEntryViewHolderType(type: ValueType): DataEntryViewHolderTypes {
            return when (type) {
                ValueType.DATE -> DataEntryViewHolderTypes.DATE
                ValueType.TIME -> DataEntryViewHolderTypes.TIME
                ValueType.DATETIME -> DataEntryViewHolderTypes.DATETIME
                else -> DataEntryViewHolderTypes.DATETIME
            }
        }
    }
}
