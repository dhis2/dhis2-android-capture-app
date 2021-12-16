package org.dhis2.data.forms.dataentry.fields.display;


import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class DisplayViewModel extends FieldViewModel {

    public static DisplayViewModel create(String id, String label, String value, String description, FlowableProcessor<RowAction> processor, String url) {
        return new AutoValue_DisplayViewModel(id, label, false, value, null, null, false, null, null, null, description, ObjectStyle.builder().build(), null, DataEntryViewHolderTypes.DISPLAY, processor, null,false, url);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_DisplayViewModel(uid(), label(), true, value(), null, null, false, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.DISPLAY, processor(), style(),activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), value(), null, null, false, null, warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.DISPLAY, processor(), style(),activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), value(), null, null, false, null, warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.DISPLAY, processor(), style(),activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), data, null, null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.DISPLAY, processor(), style(),activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), value(), null, null, isEditable, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.DISPLAY, processor(), style(),activated(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), value(), null, null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.DISPLAY, processor(), style(),isFocused, url());
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_form_display;
    }

    public int colorBG() {
        return -1;
    }
}
