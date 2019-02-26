package org.dhis2.data.forms;

import android.database.Cursor;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.event.EventStatus;

import androidx.annotation.NonNull;

import static android.text.TextUtils.isEmpty;

public class FieldViewModelUtils {
    private String uid;
    private String label;
    private ValueType valueType;
    private boolean mandatory;
    private String optionSetUid;
    private String dataValue;
    private String optionCodeName;
    private String section;
    private Boolean allowFutureDates;
    private EventStatus eventStatus;
    private String formLabel;
    private String description;

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

    public String getUid() {
        return uid;
    }

    public String getLabel() {
        return label;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public String getOptionSetUid() {
        return optionSetUid;
    }

    public String getDataValue() {
        return dataValue;
    }

    public String getOptionCodeName() {
        return optionCodeName;
    }

    public String getSection() {
        return section;
    }

    public Boolean getAllowFutureDates() {
        return allowFutureDates;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public String getFormLabel() {
        return formLabel;
    }

    public String getDescription() {
        return description;
    }
}
