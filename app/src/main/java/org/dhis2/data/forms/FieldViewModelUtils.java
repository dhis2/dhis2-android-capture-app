package org.dhis2.data.forms;

import android.content.ContentValues;
import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;

import androidx.annotation.NonNull;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.WHERE;

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

    public static Integer getOptionCount(@NonNull BriteDatabase briteDatabase,
                                         @NonNull String optionSetUid) {
        int optionCount = 0;
        try {
            Cursor countCursor = briteDatabase.query("SELECT COUNT (uid) FROM Option WHERE optionSet = ?", optionSetUid);
            if (countCursor != null) {
                if (countCursor.moveToFirst())
                    optionCount = countCursor.getInt(0);
                countCursor.close();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return optionCount;
    }

    public static ValueTypeDeviceRendering getValueTypeDeviceRendering(@NonNull BriteDatabase briteDatabase,
                                                                       @NonNull String uid) {
        ValueTypeDeviceRendering fieldRendering = null;
        Cursor rendering = briteDatabase.query("SELECT ValueTypeDeviceRendering.* FROM ValueTypeDeviceRendering " +
                "JOIN ProgramTrackedEntityAttribute ON ProgramTrackedEntityAttribute.uid = ValueTypeDeviceRendering.uid WHERE " +
                "ProgramTrackedEntityAttribute.trackedEntityAttribute = ?", uid);
        if (rendering != null) {
            if (rendering.moveToFirst())
                fieldRendering = ValueTypeDeviceRendering.create(rendering);
            rendering.close();
        }
        return fieldRendering;
    }

    public static void updateTEI(@NonNull BriteDatabase briteDatabase, String teiUid){
        String selectTei = SELECT + ALL + FROM + TrackedEntityInstanceModel.TABLE + WHERE +
                TrackedEntityInstanceModel.Columns.UID + EQUAL + QUESTION_MARK;
        Cursor teiCursor = briteDatabase.query(selectTei, teiUid);
        if (teiCursor != null && teiCursor.moveToFirst()) {
            TrackedEntityInstanceModel teiModel = TrackedEntityInstanceModel.create(teiCursor);
            ContentValues cv = teiModel.toContentValues();
            cv.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
            cv.put(TrackedEntityInstanceModel.Columns.STATE,
                    teiModel.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
            briteDatabase.update(TrackedEntityInstanceModel.TABLE, cv, TrackedEntityInstanceModel.Columns.UID + EQUAL + QUESTION_MARK, teiUid);
            teiCursor.close();
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
