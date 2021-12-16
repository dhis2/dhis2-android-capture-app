package org.dhis2.data.forms.dataentry.fields.edittext;

import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.KeyboardActionType;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class EditTextViewModel extends EditTextModel<String> {

    public abstract String renderType();

    public abstract boolean isBackgroundTransparent();

    public abstract boolean isSearchMode();

    @Nullable
    public abstract ValueTypeDeviceRendering fieldRendering();

    @Nullable
    public abstract LegendValue legendValue();

    public abstract KeyboardActionType keyBoardAction();

    @NonNull
    public static EditTextViewModel create(@NonNull String uid, @NonNull String label,
                                           @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint,
                                           @NonNull Integer lines, @NonNull ValueType valueType, @Nullable String section,
                                           @NonNull Boolean editable, @Nullable String description,
                                           @Nullable ValueTypeDeviceRendering fieldRendering, ObjectStyle objectStyle, @Nullable String fieldMask, String renderType, boolean isBackgroundTransparent,
                                           boolean isSearchMode, FlowableProcessor<RowAction> processor,
                                           @Nullable LegendValue legendValue,@Nullable String url) {

        KeyboardActionType keyboardActionType;
        if (valueType == ValueType.LONG_TEXT) {
            keyboardActionType = KeyboardActionType.ENTER;
        } else if (isSearchMode) {
            keyboardActionType = KeyboardActionType.DONE;
        } else {
            keyboardActionType = KeyboardActionType.NEXT;
        }

        return new AutoValue_EditTextViewModel(uid, label, mandatory,
                value, section, null, editable, null, description, objectStyle, fieldMask, DataEntryViewHolderTypes.EDIT_TEXT, processor, null,false, url, hint, lines,
                InputType.TYPE_CLASS_TEXT, valueType, null, null, renderType, isBackgroundTransparent, isSearchMode, fieldRendering, legendValue, keyboardActionType);
    }

    @NonNull
    @Override
    public EditTextViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, editable(), null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(), style(),activated(), url(), hint(), maxLines(), inputType(), valueType(), warning, error(), renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue(), keyBoardAction());
    }

    @NonNull
    @Override
    public EditTextViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, true, null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(), style(),activated(), url(), hint(), maxLines(), inputType(), valueType(), warning(), error,
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue(), keyBoardAction());
    }

    @NonNull
    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_EditTextViewModel(uid(), label(), true,
                value(), programStageSection(), null, editable(), null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(), style(),activated(), url(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue(), keyBoardAction());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                data, programStageSection(), null, editable(), null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(), style(),activated(), url(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue(), keyBoardAction());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, isEditable, null,
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT, processor(), style(),activated(), url(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(),
                renderType(), isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue(), keyBoardAction());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), allowFutureDate(), editable(), optionSet(),
                description(), objectStyle(), fieldMask(), DataEntryViewHolderTypes.EDIT_TEXT,
                processor(), style(),isFocused, url(), hint(), maxLines(),
                InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(), renderType(),
                isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue(), keyBoardAction());
    }


    @NonNull
    public FieldViewModel withlegendValue(LegendValue legendValue) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), allowFutureDate(), editable(), optionSet(),
                description(), objectStyle(), fieldMask(), dataEntryViewType(),
                processor(), style(),activated(), url(), hint(), maxLines(),
                InputType.TYPE_CLASS_TEXT, valueType(), warning(), error(), renderType(),
                isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue, keyBoardAction());
    }

    @NonNull
    public FieldViewModel withKeyBoardActionDone() {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), allowFutureDate(), editable(), optionSet(),
                description(), objectStyle(), fieldMask(), dataEntryViewType(),
                processor(), style(),activated(), url(), hint(), maxLines(),
                inputType(), valueType(), warning(), error(), renderType(),
                isBackgroundTransparent(), isSearchMode(), fieldRendering(), legendValue(), KeyboardActionType.DONE);
    }

    @Override
    public int getLayoutId() {
        if (valueType() == ValueType.LONG_TEXT) {
            return R.layout.form_long_text_custom;
        }
        return R.layout.form_edit_text_custom;
    }

    public void onTextFilled(String value, String error) {
        processor().onNext(new RowAction(
                uid(),
                value,
                false,
                null,
                null,
                null,
                error,
                ActionType.ON_SAVE
        ));
    }

    public boolean isLongText() {
        return valueType() == ValueType.LONG_TEXT;
    }

    @Override
    public boolean equals(FieldUiModel o) {
        return super.equals(o)
                && (o instanceof EditTextViewModel && ((EditTextViewModel) o).legendValue() == legendValue());
    }
}
