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
    public static RadioButtonViewModel fromRawValue(@NonNull String id, int layoutId, @NonNull String label, @NonNull ValueType type,
                                                    @NonNull Boolean mandatory, @Nullable String value, @Nullable String section,
                                                    Boolean editable, @Nullable String description, ObjectStyle objectStyle, ValueTypeRenderingType renderingType, Boolean isBackgroundTransparent, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        if (value == null) {
            return new AutoValue_RadioButtonViewModel(id, layoutId, label, null, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, null,false, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED.toString()) || value.toLowerCase(Locale.US).equals(Value.YES.toString())) {
            return new AutoValue_RadioButtonViewModel(id, layoutId, label, Value.CHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor,null, false, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else if (value.toLowerCase(Locale.US).equals(Value.UNCHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, layoutId, label, Value.UNCHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, null,false, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED_NO.toString()) || value.toLowerCase(Locale.US).equals(Value.NO.toString())) {
            return new AutoValue_RadioButtonViewModel(id, layoutId, label, Value.CHECKED_NO.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, null,false, mandatory, type, renderingType, isBackgroundTransparent, isSearchMode);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_RadioButtonViewModel(uid(), layoutId(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), true, valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_RadioButtonViewModel(uid(), layoutId(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_RadioButtonViewModel(uid(), layoutId(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_RadioButtonViewModel(uid(), layoutId(), label(), data, programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_RadioButtonViewModel(uid(), layoutId(), label(), value(), programStageSection(), allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),activated(), mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_RadioButtonViewModel(uid(), layoutId(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(), style(),isFocused, mandatory(), valueType(), renderingType(), isBackgroundTransparent(), isSearchMode());
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
