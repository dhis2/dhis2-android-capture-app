package org.dhis2.data.forms;

import android.database.Cursor;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.event.EventStatus;

import androidx.annotation.NonNull;

import static android.text.TextUtils.isEmpty;

public class FieldViewModelUtils {
    public String uid;
    public String label;
    public ValueType valueType;
    public boolean mandatory;
    public String optionSetUid;
    public String dataValue;
    public String optionCodeName;
    public String section;
    public Boolean allowFutureDates;
    public EventStatus eventStatus;
    public String formLabel;
    public String description;

    public FieldViewModelUtils(@NonNull Cursor cursor) {
        uid = cursor.getString(0);
        label = cursor.getString(1);
        valueType = ValueType.valueOf(cursor.getString(2));
        mandatory = cursor.getInt(3) == 1;
        optionSetUid = cursor.getString(4);
        dataValue = cursor.getString(5);
        optionCodeName = cursor.getString(6);
        section = cursor.getString(7);
        allowFutureDates = cursor.getInt(8) == 1;
        eventStatus = EventStatus.valueOf(cursor.getString(9));
        formLabel = cursor.getString(10);
        description = cursor.getString(11);
        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }
    }
}
