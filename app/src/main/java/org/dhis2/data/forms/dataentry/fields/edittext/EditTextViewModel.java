package org.dhis2.data.forms.dataentry.fields.edittext;

import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class EditTextViewModel extends EditTextModel<String> {

    @Nullable
    public abstract ValueTypeDeviceRendering fieldRendering();

    @NonNull
    public static EditTextViewModel create(@NonNull String uid, @NonNull String label,
                                           @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint,
                                           @NonNull Integer lines, @NonNull ValueType valueType, @Nullable String section,
                                           @NonNull Boolean editable, @Nullable String description,
                                           @Nullable ValueTypeDeviceRendering fieldRendering, ObjectStyle objectStyle, @Nullable String fieldMask) {
        return new AutoValue_EditTextViewModel(uid, label, mandatory,
                value, section, null, editable, null, description, objectStyle, fieldMask,hint, lines,
                InputType.TYPE_CLASS_TEXT, valueType, null, null, fieldRendering);
    }

    @NonNull
    @Override
    public EditTextViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, editable(), null,
                description(), objectStyle(), fieldMask(), hint(), maxLines(), inputType(), valueType(), warning, error(), fieldRendering());
    }

    @NonNull
    @Override
    public EditTextViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, true, null,
                description(), objectStyle(), fieldMask(), hint(), maxLines(), inputType(), valueType(), warning(), error,
                fieldRendering());
    }

    @NonNull
    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_EditTextViewModel(uid(), label(), true,
                value(), programStageSection(), null, editable(), null,
                description(), objectStyle(), fieldMask(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                fieldRendering());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                data, programStageSection(), null, false, null,
                description(), objectStyle(), fieldMask(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                fieldRendering());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, isEditable, null,
                description(), objectStyle(), fieldMask(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                fieldRendering());    }
}
