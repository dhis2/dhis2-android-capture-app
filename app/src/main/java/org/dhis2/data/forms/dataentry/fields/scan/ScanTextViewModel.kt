package org.dhis2.data.forms.dataentry.fields.scan

import com.google.auto.value.AutoValue
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.utils.Preconditions
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering

@AutoValue
abstract class ScanTextViewModel : FieldViewModel() {

    abstract val fieldRendering: ValueTypeDeviceRendering?

    abstract val hint: String?

    companion object {

        @JvmStatic
        fun create(
            id: String,
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
            processor: FlowableProcessor<RowAction>
        ): FieldViewModel =
            AutoValue_ScanTextViewModel(
                id,
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
                processor,
                null,
                false,
                fieldRendering,
                hint,
                isBackgroundTransparent,
                isSearchMode
            )
    }

    override fun setMandatory(): FieldViewModel =
        AutoValue_ScanTextViewModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            fieldRendering,
            hint,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withError(error: String): FieldViewModel =
        AutoValue_ScanTextViewModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            fieldRendering,
            hint,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withWarning(warning: String): FieldViewModel =
        AutoValue_ScanTextViewModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            fieldRendering,
            hint,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withValue(data: String?): FieldViewModel =
        AutoValue_ScanTextViewModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            fieldRendering,
            hint,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withEditMode(isEditable: Boolean): FieldViewModel =
        AutoValue_ScanTextViewModel(
            uid(),
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
            processor(),
            style(),
            activated(),
            fieldRendering,
            hint,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun withFocus(isFocused: Boolean): FieldViewModel =
        AutoValue_ScanTextViewModel(
            uid(),
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
            processor(),
            style(),
            isFocused,
            fieldRendering,
            hint,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override val layoutId: Int
        get() = R.layout.form_scan

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
}
