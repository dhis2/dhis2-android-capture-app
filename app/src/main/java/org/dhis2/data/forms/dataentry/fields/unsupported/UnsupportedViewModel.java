package org.dhis2.data.forms.dataentry.fields.unsupported;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class UnsupportedViewModel extends FieldViewModel {

    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, FlowableProcessor<RowAction> processor, String url) {
        return new AutoValue_UnsupportedViewModel(id, label, false, value, section, null, false, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.UNSUPPORTED, processor, null, false, url);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), style(), activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), style(), activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), style(), activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, data, programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), style(), activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), style(), activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), style(), isFocused, url());
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_unsupported;
    }
}
