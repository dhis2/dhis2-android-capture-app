package org.dhis2.utils

import android.database.Cursor

import com.squareup.sqlbrite2.BriteDatabase

import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.OptionModel
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel

/**
 * QUADRAM. Created by ppajuelo on 25/09/2018.
 */

object ValueUtils {

    /**
     * @param briteDatabase access to database
     * @param cursor        cursor of the original TEAV
     * @return Returns a trackedEntityAttributeValueModel which value has been parse for valueType orgunit uid or optionSet code/name
     */
    fun transform(briteDatabase: BriteDatabase, cursor: Cursor): TrackedEntityAttributeValueModel {
        var teAttrValue = TrackedEntityAttributeValueModel.create(cursor)
        val valueTypeIndex = cursor.getColumnIndex("valueType")
        val optionSetIndex = cursor.getColumnIndex("optionSet")
        if (cursor.getString(valueTypeIndex) == ValueType.ORGANISATION_UNIT.name) {
            val orgUnitUid = cursor.getString(cursor.getColumnIndex("value"))
            briteDatabase.query("SELECT OrganisationUnit.displayName FROM OrganisationUnit WHERE OrganisationUnit.uid = ?", orgUnitUid).use { orgUnitCursor ->
                if (orgUnitCursor != null && orgUnitCursor.moveToFirst()) {
                    val orgUnitName = orgUnitCursor.getString(0)
                    teAttrValue = TrackedEntityAttributeValueModel.builder()
                            .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                            .lastUpdated(teAttrValue.lastUpdated())
                            .created(teAttrValue.created())
                            .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                            .value(orgUnitName)
                            .build()
                }
            }
        } else if (cursor.getString(optionSetIndex) != null) {
            val optionSet = cursor.getString(optionSetIndex)
            val optionCode = cursor.getString(cursor.getColumnIndex("value"))
            briteDatabase.query("SELECT * FROM Option WHERE optionSet = ?", optionSet).use { optionsCursor ->
                if (optionsCursor != null && optionsCursor.moveToFirst()) {
                    for (i in 0 until optionsCursor.count) {
                        val optionModel = OptionModel.create(optionsCursor)
                        if (optionModel.code() == optionCode || optionModel.name() == optionCode) {
                            teAttrValue = TrackedEntityAttributeValueModel.builder()
                                    .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                                    .lastUpdated(teAttrValue.lastUpdated())
                                    .created(teAttrValue.created())
                                    .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                                    .value(optionModel.displayName())
                                    .build()
                        }
                        optionsCursor.moveToNext()
                    }
                }
            }
        }
        return teAttrValue
    }

    fun optionSetCodeToDisplayName(briteDatabase: BriteDatabase, optionSet: String, optionSetCode: String): String? {
        var displayName: String? = optionSetCode
        briteDatabase.query("SELECT * FROM Option WHERE optionSet = ? AND code = ? LIMIT 1", optionSet, optionSetCode).use { optionsCursor ->
            if (optionsCursor != null && optionsCursor.moveToFirst()) {
                val optionModel = OptionModel.create(optionsCursor)
                displayName = optionModel.displayName()
            }
        }
        return displayName
    }

    fun orgUnitUidToDisplayName(briteDatabase: BriteDatabase, value: String): String {
        var displayName = value
        briteDatabase.query("SELECT OrganisationUnit.displayName FROM OrganisationUnit WHERE OrganisationUnit.uid = ?", value).use { orgUnitCursor ->
            if (orgUnitCursor != null && orgUnitCursor.moveToFirst()) {
                displayName = orgUnitCursor.getString(0)
            }
        }
        return displayName
    }
}
