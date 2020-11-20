package org.dhis2.data.forms.dataentry.fields.edittext;

import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class EditTextViewModel extends EditTextModel<String> {

    public boolean activated = false;

    public abstract String renderType();

    public abstract boolean isBackgroundTransparent();

    public abstract boolean isSearchMode();

    @Nullable
    public abstract ValueTypeDeviceRendering fieldRendering();

    @NonNull
    public static EditTextViewModel create(@NonNull String uid, @NonNull String label,
                                           @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint,
                                           @NonNull Integer lines, @NonNull ValueType valueType, @Nullable String section,
                                           @NonNull Boolean editable, @Nullable String description,
                                           @Nullable ValueTypeDeviceRendering fieldRendering, ObjectStyle objectStyle, @Nullable String fieldMask, String renderType, boolean isBackgroundTransparent, boolean isSearchMode) {
        return new AutoValue_EditTextViewModel(uid, label, mandatory,
                value, section, null, editable, null, description, objectStyle, fieldMask, DataEntryViewHolderTypes.EDIT_TEXT, null, hint, lines,
                InputType.TYPE_CLASS_TEXT, valueType, null, null, renderType, isBackgroundTransparent, isSearchMode, fieldRendering);
    }

    @NonNull
    public static EditTextViewModel create(@NonNull String uid, @NonNull String label,
                                           @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint,
                                           @NonNull Integer lines, @NonNull ValueType valueType, @Nullable String section,
                                           @NonNull Boolean editable, @Nullable String description,
                                           @Nullable ValueTypeDeviceRendering fieldRendering, ObjectStyle objectStyle, @Nullable String fieldMask, String renderType, boolean isBackgroundTransparent,
                                           boolean isSearchMode, FlowableProcessor<RowAction> processor) {
        return new AutoValue_EditTextViewModel(uid, label, mandatory,
                value, section, null, editable, null, description, objectStyle, fieldMask, DataEntryViewHolderTypes.EDIT_TEXT, processor, hint, lines,
                InputType.TYPE_CLASS_TEXT, valueType, null, null, renderType, isBackgroundTransparent, isSearchMode, fieldRendering);
    }

    @NonNull
    @Override
    public EditTextViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, editable(), null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(),hint(), maxLines(), inputType(), valueType(), warning, error(), renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering());
    }

    @NonNull
    @Override
    public EditTextViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, true, null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(),hint(), maxLines(), inputType(), valueType(), warning(), error,
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering());
    }

    @NonNull
    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_EditTextViewModel(uid(), label(), true,
                value(), programStageSection(), null, editable(), null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                data, programStageSection(), null, false, null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, isEditable, null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(),hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering());
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_edit_text_custom;
    }

    public void onActivate() {
        activated = true;
    }

    public void onDeactivate() {
        activated = false;
    }

    public void onTextFilled(String value) {
        processor().onNext(RowAction.create(uid(), value, getAdapterPosition()));
    }

    public boolean isLongText(){
        return valueType() == ValueType.LONG_TEXT;
    }
}
