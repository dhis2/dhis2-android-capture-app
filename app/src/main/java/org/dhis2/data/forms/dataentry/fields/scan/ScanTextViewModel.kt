package org.dhis2.data.forms.dataentry.fields.scan

import com.google.auto.value.AutoValue
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering

@AutoValue
abstract class ScanTextViewModel : FieldViewModel() {

    var activated: Boolean = false

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
            isSearchMode: Boolean
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
                null,
                fieldRendering,
                hint,
                isBackgroundTransparent,
                isSearchMode
            )

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
            false,
            optionSet(),
            warning(),
            error(),
            description(),
            objectStyle(),
            null,
            DataEntryViewHolderTypes.SCAN_CODE,
            processor(),
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
            fieldRendering,
            hint,
            isBackgroundTransparent(),
            isSearchMode()
        )

    override fun getLayoutId(): Int {
        return R.layout.form_scan
    }

    abstract fun isBackgroundTransparent(): Boolean
    abstract fun isSearchMode(): Boolean

    fun onActivate() {
        activated = true
    }

    fun onDeactivate() {
        activated = false
    }

    fun onScanSelected(value: String?) {
        processor()?.onNext(RowAction.create(uid(), value, adapterPosition))
    }
}
