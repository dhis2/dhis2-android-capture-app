package org.dhis2.data.forms.dataentry.fields.image;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */
@AutoValue
public abstract class ImageViewModel extends FieldViewModel {

    public static ImageViewModel create(String id, String label, String optionSet, String value, String section, Boolean editable, Boolean mandatory) {
        return new AutoValue_ImageViewModel(id, label, mandatory, value, section, true, editable, optionSet, null, null);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_ImageViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_ImageViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_ImageViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error());
    }
}
