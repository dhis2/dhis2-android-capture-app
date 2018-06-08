package com.dhis2.data.forms.dataentry.fields.unsupported;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UnsupportedViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value,String section) {
        return new AutoValue_UnsupportedViewModel(id, label, mandatory, value, section, null,false,null);
    }
}
