package org.dhis2.data.forms.dataentry.fields.file;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FileViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, String description, ObjectStyle objectStyle) {
        return new AutoValue_FileViewModel(id, label, mandatory, value, section, null,
                true, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.BUTTON);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_FileViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON);
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_FileViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_FileViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON);
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_FileViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON);
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_FileViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON);
    }


    @Override
    public int getLayoutId() {
        return R.layout.form_button;
    }

    /*
    button.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                View nextView;
                if ((nextView = v.focusSearch(FOCUS_DOWN)) != null)
                    nextView.requestFocus();
            } else
                itemView.setBackgroundColor(Color.WHITE);

        });
     */
}
