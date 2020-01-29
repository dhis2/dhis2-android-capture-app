package org.dhis2.data.forms.dataentry.fields.scan

import com.google.auto.value.AutoValue
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering

@AutoValue
abstract class ScanTextViewModel : FieldViewModel() {

    abstract val fieldRendering: ValueTypeDeviceRendering?

    companion object {
        @JvmStatic
        fun create(
            id: String,
            label: String?,
            mandatory: Boolean?,
            value: String?,
            section: String?,
            editable: Boolean?,
            description: String?,
            objectStyle: ObjectStyle?,
            fieldRendering: ValueTypeDeviceRendering?
        ): FieldViewModel =
            AutoValue_ScanTextViewModel(
                id,
                label,
                mandatory,
                value,
                section,
                null,
                editable,
                null,
                null,
                null,
                description,
                objectStyle,
                null,
                fieldRendering
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
            fieldRendering
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
            fieldRendering
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
            fieldRendering
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
            fieldRendering
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
            fieldRendering
        )
}
