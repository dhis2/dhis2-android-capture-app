package com.dhis2.data.forms.dataentry.fields.image;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */
@AutoValue
public abstract class ImageViewModel extends FieldViewModel {

    public static ImageViewModel create(String id, String label, String optionSet, String value, String section) {
        return new AutoValue_ImageViewModel(id, label, false,value,section,true,true,optionSet);
    }
}
