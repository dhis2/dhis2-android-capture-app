package org.dhis2.data.forms.dataentry.fields.radiobutton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.RecyclerViewUiEvents;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;

import java.util.Locale;
import java.util.Objects;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class RadioButtonViewModel extends FieldViewModel {

    public enum Value {
        CHECKED("true"), CHECKED_NO("false"), UNCHECKED(""), NO("no"), YES("yes");

        @NonNull
        private final String value;

        Value(@NonNull String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @NonNull
    public abstract Boolean mandatory();

    @NonNull
    public abstract ValueType valueType();

    @Nullable
    public abstract ValueTypeRenderingType renderingType();

    @NonNull
    public static RadioButtonViewModel fromRawValue(@NonNull String id, @NonNull String label, @NonNull ValueType type,
                                                    @NonNull Boolean mandatory, @Nullable String value, @Nullable String section,
                                                    Boolean editable, @Nullable String description, ObjectStyle objectStyle, ValueTypeRenderingType renderingType, Boolean isBackgroundTransparent, FlowableProcessor<RowAction> processor, boolean isSearchMode, String url) {
        if (value == null) {
            return new AutoValue_RadioButtonViewModel(id, label, null, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, null,false, url, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED.toString()) || value.toLowerCase(Locale.US).equals(Value.YES.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor,null, false, url, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else if (value.toLowerCase(Locale.US).equals(Value.UNCHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.UNCHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, null,false, url, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED_NO.toString()) || value.toLowerCase(Locale.US).equals(Value.NO.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED_NO.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, null,false, url, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), url(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), url(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), url(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), data, programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), url(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), url(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),isFocused, url(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @Override
    public int getLayoutId() {
        if (renderingType() == ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS ||
                renderingType() == ValueTypeRenderingType.DEFAULT ||
                renderingType() == ValueTypeRenderingType.VERTICAL_RADIOBUTTONS ||
                (renderingType() == ValueTypeRenderingType.TOGGLE && valueType() != ValueType.TRUE_ONLY)
        ) {
            return R.layout.form_radio_button_horizontal;
        } else if (renderingType() == ValueTypeRenderingType.HORIZONTAL_CHECKBOXES ||
                renderingType() == ValueTypeRenderingType.VERTICAL_CHECKBOXES) {
            return R.layout.form_check_button;
        } else if (renderingType() == ValueTypeRenderingType.TOGGLE && valueType() == ValueType.TRUE_ONLY) {
            return R.layout.form_toggle;
        }
        return R.layout.form_yes_no;
    }

    public void onValueChanged(Boolean value) {
        onItemClick();

        String result = null;
        if (value != null) {
            if (value() == null || !Objects.equals(value(), String.valueOf(value)))
                result = String.valueOf(value);
        }

        if (processor() == null || Objects.equals(result, value())) return;
        Objects.requireNonNull(processor()).onNext(new RowAction(
                uid(),
                result,
                false,
                null,
                null,
                null,
                null,
                ActionType.ON_SAVE));
    }

    public abstract boolean isBackgroundTransparent();

    public abstract boolean isSearchMode();

    public void onClear() {
        onValueChanged(null);
    }

    public boolean isClearable() {
        return editable() && value() != null;
    }

    public boolean isNegativeChecked() {
        return value() != null && !Boolean.parseBoolean(value());
    }

    public boolean isAffirmativeChecked() {
        return Boolean.parseBoolean(value());
    }

    public void onDescriptionClick() {
        callback.recyclerViewUiEvents(new RecyclerViewUiEvents.ShowDescriptionLabelDialog(label(), description()));
    }

    @Override
    public boolean equals(FieldUiModel o) {
        if (o instanceof RadioButtonViewModel) {
            RadioButtonViewModel oRadioButton = (RadioButtonViewModel) o;
            return super.equals(o) &&
                    this.valueType() == oRadioButton.valueType() &&
                    this.renderingType() == oRadioButton.renderingType();
        } else {
            return super.equals(o);
        }
    }
}
