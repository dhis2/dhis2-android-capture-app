package org.dhis2.data.forms.dataentry.fields.radiobutton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;

import java.util.Locale;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */
@AutoValue
public abstract class RadioButtonViewModel extends FieldViewModel {

    public enum Value {
        CHECKED("true"), CHECKED_NO("false"), UNCHECKED("");

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
                                                    Boolean editable, @Nullable String description, ObjectStyle objectStyle, ValueTypeRenderingType renderingType, Boolean isBackgroundTransparent) {
        if (value == null) {
            return new AutoValue_RadioButtonViewModel(id, label, null, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, null,mandatory, type, renderingType, isBackgroundTransparent);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, null,mandatory, type, renderingType, isBackgroundTransparent);
        } else if (value.toLowerCase(Locale.US).equals(Value.UNCHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.UNCHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, null, mandatory, type, renderingType, isBackgroundTransparent);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED_NO.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED_NO.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, null, mandatory, type, renderingType, isBackgroundTransparent);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }

    @NonNull
    public static RadioButtonViewModel fromRawValue(@NonNull String id, @NonNull String label, @NonNull ValueType type,
                                                    @NonNull Boolean mandatory, @Nullable String value, @Nullable String section,
                                                    Boolean editable, @Nullable String description, ObjectStyle objectStyle, ValueTypeRenderingType renderingType, Boolean isBackgroundTransparent, FlowableProcessor<RowAction> processor) {
        if (value == null) {
            return new AutoValue_RadioButtonViewModel(id, label, null, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor,mandatory, type, renderingType, isBackgroundTransparent);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor,mandatory, type, renderingType, isBackgroundTransparent);
        } else if (value.toLowerCase(Locale.US).equals(Value.UNCHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.UNCHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, mandatory, type, renderingType, isBackgroundTransparent);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED_NO.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED_NO.toString(), section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.YES_NO, processor, mandatory, type, renderingType, isBackgroundTransparent);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(),mandatory(), valueType(), renderingType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(),mandatory(), valueType(), renderingType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(),mandatory(), valueType(), renderingType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), data, programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(),mandatory(), valueType(), renderingType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.YES_NO, processor(),mandatory(), valueType(), renderingType(), isBackgroundTransparent());
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_yes_no;
    }

    public abstract boolean isBackgroundTransparent();
}
