package org.dhis2.usescases.reservedValue;


import android.database.Cursor;

import org.hisp.dhis.android.core.D2;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

public class ReservedValueRepositoryImpl implements ReservedValueRepository {

    private static final String SELECT_DATA_ELEMENTS = "SELECT TEA.uid, TEA.displayName, TEA.pattern, count(rv.ownerUid)reservedValue, ou.uid, ou.displayName " +
            "FROM TrackedEntityAttribute AS TEA " +
            "LEFT JOIN TrackedEntityAttributeReservedValue AS rv ON rv.ownerUid = TEA.uid " +
            "LEFT JOIN OrganisationUnit ou ON ou.uid = rv.organisationUnit " +
            "WHERE generated = 1 " +
            "GROUP BY TEA.uid, ou.uid " +
            "ORDER BY TEA.displayName";

    private final D2 d2;

    public ReservedValueRepositoryImpl(D2 d2) {
        this.d2 = d2;
    }

    @Override
    public Flowable<List<ReservedValueModel>> getDataElements() {
        return Flowable.fromCallable(() -> {
            List<ReservedValueModel> reservedValueModels = new ArrayList<>();
            try (Cursor cursor = d2.databaseAdapter().rawQuery(SELECT_DATA_ELEMENTS, null)) {
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        String uid = cursor.getString(0);
                        String displayName = cursor.getString(1);
                        String pattern = cursor.getString(2);
                        boolean patternCointainsOU = pattern.contains("OU");
                        int reservedValues = cursor.getInt(3);
                        reservedValueModels.add(
                                ReservedValueModel.create(
                                        uid,
                                        displayName,
                                        patternCointainsOU,
                                        cursor.getString(4),
                                        cursor.getString(5),
                                        reservedValues)
                        );
                    } while (cursor.moveToNext());
                }
            }
            return reservedValueModels;
        });
    }
}
