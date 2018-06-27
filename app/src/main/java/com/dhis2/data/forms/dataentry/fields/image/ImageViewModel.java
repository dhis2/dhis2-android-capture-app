package com.dhis2.data.forms.dataentry.fields.image;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */
@AutoValue
public abstract class ImageViewModel extends FieldViewModel {

    public static ImageViewModel create(String id, String label, String optionSet, String value, String section) {
        return new AutoValue_ImageViewModel(id, label, false,value,section,true,true,optionSet,null,null);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_ImageViewModel(uid(),label(),true,value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_ImageViewModel(uid(),label(),true,value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_ImageViewModel(uid(),label(),true,value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning,error());
    }
}
