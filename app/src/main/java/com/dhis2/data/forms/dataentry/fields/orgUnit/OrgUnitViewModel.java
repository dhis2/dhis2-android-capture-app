package com.dhis2.data.forms.dataentry.fields.orgUnit;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * Created by ppajuelo on 19/03/2018.
 */
@AutoValue
public abstract class OrgUnitViewModel extends FieldViewModel {

    public static FieldViewModel create(String id, String label, Boolean mandatory, String value) {
        return new AutoValue_OrgUnitViewModel(id, label, mandatory, value);
    }
}
