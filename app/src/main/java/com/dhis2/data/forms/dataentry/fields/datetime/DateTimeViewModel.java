package com.dhis2.data.forms.dataentry.fields.datetime;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.EditableFieldViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;

/**
 * Created by frodriguez on 1/24/2018.
 */

public abstract class DateTimeViewModel extends EditableFieldViewModel<String> {

    @NonNull
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value) {
        return null;
    }
}
