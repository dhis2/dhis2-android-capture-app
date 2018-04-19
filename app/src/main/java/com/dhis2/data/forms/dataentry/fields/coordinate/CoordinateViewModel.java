package com.dhis2.data.forms.dataentry.fields.coordinate;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class CoordinateViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value,String section) {
        return new AutoValue_CoordinateViewModel(id, label, mandatory, value,section);
    }
}
