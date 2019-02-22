package org.dhis2.utils;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

/**
 * QUADRAM. Created by ppajuelo on 25/09/2018.
 */

public class ValueUtils {

    private ValueUtils(){
        // hide public constructor
    }

    private static TrackedEntityAttributeValueModel valueOrgUnit(BriteDatabase briteDatabase,
                                                                 Cursor cursor,
                                                                 TrackedEntityAttributeValueModel teAttrValue) {
        String orgUnitUid = cursor.getString(cursor.getColumnIndex("VALUE"));
        Cursor orgUnitCursor = briteDatabase.query("SELECT OrganisationUnit.displayName FROM OrganisationUnit WHERE OrganisationUnit.uid = ?", orgUnitUid);
        if (orgUnitCursor != null && orgUnitCursor.moveToFirst()) {
            String orgUnitName = orgUnitCursor.getString(0);
            teAttrValue = TrackedEntityAttributeValueModel.builder()
                    .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                    .lastUpdated(teAttrValue.lastUpdated())
                    .created(teAttrValue.created())
                    .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                    .value(orgUnitName)
                    .build();
            orgUnitCursor.close();
        }
        return teAttrValue;
    }

    private static TrackedEntityAttributeValueModel valueOptionSet(BriteDatabase briteDatabase,
                                                                   Cursor cursor,
                                                                   TrackedEntityAttributeValueModel teAttrValue,
                                                                   int optionSetIndex) {
        String optionSet = cursor.getString(optionSetIndex);
        String optionCode = cursor.getString(cursor.getColumnIndex("VALUE"));
        Cursor optionsCursor = briteDatabase.query("SELECT * FROM Option WHERE optionSet = ?", optionSet);
        if (optionsCursor != null && optionsCursor.moveToFirst()) {
            for (int i = 0; i < optionsCursor.getCount(); i++) {
                OptionModel optionModel = OptionModel.create(optionsCursor);
                if (optionModel.code().equals(optionCode) || optionModel.name().equals(optionCode)) {
                    teAttrValue = TrackedEntityAttributeValueModel.builder()
                            .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                            .lastUpdated(teAttrValue.lastUpdated())
                            .created(teAttrValue.created())
                            .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                            .value(optionModel.displayName())
                            .build();
                }
                optionsCursor.moveToNext();
            }
            optionsCursor.close();
        }
        return teAttrValue;
    }

    /**
     * @param briteDatabase access to database
     * @param cursor        cursor of the original TEAV
     * @return Returns a trackedEntityAttributeValueModel which VALUE has been parse for valueType orgunit uid or optionSet code/name
     */
    public static TrackedEntityAttributeValueModel transform(BriteDatabase briteDatabase, Cursor cursor) {
        TrackedEntityAttributeValueModel teAttrValue = TrackedEntityAttributeValueModel.create(cursor);
        int valueTypeIndex = cursor.getColumnIndex("valueType");
        int optionSetIndex = cursor.getColumnIndex("optionSet");
        if (cursor.getString(valueTypeIndex).equals(ValueType.ORGANISATION_UNIT.name())) {
            valueOrgUnit(briteDatabase, cursor, teAttrValue);
        } else if (cursor.getString(optionSetIndex) != null) {
            valueOptionSet(briteDatabase, cursor, teAttrValue, optionSetIndex);
        }
        return teAttrValue;
    }

    public static String optionSetCodeToDisplayName(BriteDatabase briteDatabase, String optionSet, String optionSetCode) {
        String displayName = optionSetCode;
        Cursor optionsCursor = briteDatabase.query("SELECT * FROM Option WHERE optionSet = ? AND code = ? LIMIT 1", optionSet, optionSetCode);
        if (optionsCursor != null && optionsCursor.moveToFirst()) {
            OptionModel optionModel = OptionModel.create(optionsCursor);
            displayName = optionModel.displayName();
            optionsCursor.close();
        }
        return displayName;
    }

    public static String orgUnitUidToDisplayName(BriteDatabase briteDatabase, String value) {
        String displayName = value;
        Cursor orgUnitCursor = briteDatabase.query("SELECT OrganisationUnit.displayName FROM OrganisationUnit WHERE OrganisationUnit.uid = ?", value);
        if (orgUnitCursor != null && orgUnitCursor.moveToFirst()) {
            displayName = orgUnitCursor.getString(0);
            orgUnitCursor.close();
        }
        return displayName;
    }
}