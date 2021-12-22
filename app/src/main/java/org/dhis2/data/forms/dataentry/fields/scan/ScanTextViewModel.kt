package org.dhis2.data.forms.dataentry.fields.scan

import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.auto.value.AutoValue
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.style.FormUiModelStyle
import org.dhis2.utils.Preconditions
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering

@AutoValue
abstract class ScanTextViewModel : FieldViewModel() {

    abstract val fieldRendering: ValueTypeDeviceRendering?

    companion object {

        @JvmStatic
        fun create(
            id: String,
            layoutId: Int,
            label: String?,
            mandatory: Boolean?,
            value: String?,
            section: String?,
            editable: Boolean?,
            optionSet: String?,
            description: String?,
            objectStyle: ObjectStyle?,
            fieldRendering: ValueTypeDeviceRendering?,
            hint: String?,
            isBackgroundTransparent: Boolean,
            isSearchMode: Boolean,
            style: FormUiModelStyle,
            valueType: ValueType,
            url: String?
        ): FieldViewModel =
            AutoValue_ScanTextViewModel(
                id,
                layoutId,
                label,
                mandatory,
                value,
                section,
                null,
                editable,
                optionSet,
                null,
                null,
                description,
                objectStyle,
                null,
                DataEntryViewHolderTypes.SCAN_CODE,
                style,
                hint,
                false,
                valueType,
                url,
                fieldRendering,
                isBackgroundTransparent,
                isSearchMode
            )
    }

    override fun setMandatory(): FieldViewModel =
        AutoValue_ScanTextViewModel(
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
            DataEntryViewHolderTypes.SCAN_CODE,
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            fieldRendering,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withError(error: String): FieldViewModel =
        AutoValue_ScanTextViewModel(
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
            DataEntryViewHolderTypes.SCAN_CODE,
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            fieldRendering,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withWarning(warning: String): FieldViewModel =
        AutoValue_ScanTextViewModel(
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
            DataEntryViewHolderTypes.SCAN_CODE,
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            fieldRendering,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withValue(data: String?): FieldViewModel =
        AutoValue_ScanTextViewModel(
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
            DataEntryViewHolderTypes.SCAN_CODE,
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            fieldRendering,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withEditMode(isEditable: Boolean): FieldViewModel =
        AutoValue_ScanTextViewModel(
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
            DataEntryViewHolderTypes.SCAN_CODE,
            style(),
            hint(),
            activated(),
            valueType(),
            url(),
            fieldRendering,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withFocus(isFocused: Boolean): FieldViewModel =
        AutoValue_ScanTextViewModel(
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
            fieldMask(),
            DataEntryViewHolderTypes.SCAN_CODE,
            style(),
            hint(),
            isFocused,
            valueType(),
            url(),
            fieldRendering,
            isBackgroundTransparent(),
            isSearchMode()
        )

    abstract fun isBackgroundTransparent(): Boolean

    abstract fun isSearchMode(): Boolean

    fun onScanSelected(value: String?) {
        if (valueHasChanged(value)) {
            callback.intent(
                FormIntent.OnSave(
                    uid = uid(),
                    value = value,
                    valueType = null,
                    fieldMask = fieldMask()
                )
            )
        }
    }

    fun scan() {
        if (value().isNullOrEmpty()) {
            onItemClick()
            callback.recyclerViewUiEvents(
                RecyclerViewUiEvents.ScanQRCode(
                    uid(),
                    optionSet(),
                    fieldRendering?.type()
                )
            )
        } else {
            callback.recyclerViewUiEvents(
                RecyclerViewUiEvents.DisplayQRCode(
                    uid(),
                    optionSet(),
                    value()!!,
                    fieldRendering?.type(),
                    editable
                )
            )
        }
    }

    private fun valueHasChanged(newValue: String?): Boolean {
        return !Preconditions.equals(newValue, value()) || error() != null
    }

    fun canShowDeleteButton(): Boolean = value() != null && editable

    fun onClearValue() {
        onItemClick()
        onScanSelected(null)
    }

    fun onTextChanged(text: CharSequence?) {
        super.onTextChange(
            when {
                text?.isEmpty() == true -> null
                else -> text?.toString()
            }
        )
    }

    fun onFocusChanged(hasFocus: Boolean, textView: View) {
        val text = (textView as TextInputEditText).text.toString()
        if (!hasFocus) {
            onScanSelected(
                when {
                    text.isEmpty() -> null
                    else -> text
                }
            )
        }
    }
}
