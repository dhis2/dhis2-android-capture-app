package org.dhis2.usescases.reservedValue;


import com.squareup.sqlbrite2.BriteDatabase;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static org.dhis2.data.database.SqlConstants.TE_ATTR_TABLE;

public class ReservedValueRepositoryImpl implements ReservedValueRepository {

    private static final String SELECT_DATA_ELEMENTS = "SELECT TEA.uid, TEA.displayName, TEA.pattern, count(rv.ownerUid)reservedValue, ou.uid, ou.displayName " +
            "FROM TrackedEntityAttribute AS TEA " +
            "LEFT JOIN TrackedEntityAttributeReservedValue AS rv ON rv.ownerUid = TEA.uid " +
            "LEFT JOIN OrganisationUnit ou ON ou.uid = rv.organisationUnit " +
            "WHERE generated = 1 " +
            "GROUP BY TEA.uid, ou.uid " +
            "ORDER BY TEA.displayName";

    private final BriteDatabase briteDatabase;

    public ReservedValueRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Flowable<List<ReservedValueModel>> getDataElements() {
        return briteDatabase.createQuery(TE_ATTR_TABLE, SELECT_DATA_ELEMENTS)
                .mapToList(cursor -> {
                    String uid = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    String pattern = cursor.getString(2);
                    boolean patternCointainsOU = pattern.contains("OU");
                    int reservedValues = cursor.getInt(3);
                    return ReservedValueModel.create(uid, displayName, patternCointainsOU, cursor.getString(4), cursor.getString(5), reservedValues);
                }).toFlowable(BackpressureStrategy.LATEST);
    }
}
