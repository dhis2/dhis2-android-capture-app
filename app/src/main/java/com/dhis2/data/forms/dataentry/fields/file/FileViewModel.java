package com.dhis2.data.forms.dataentry.fields.file;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * Created by ppajuelo on 19/03/2018.
 */

@AutoValue
public abstract class FileViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value) {
        return new AutoValue_FileViewModel(id, label, mandatory, value);
    }
}
