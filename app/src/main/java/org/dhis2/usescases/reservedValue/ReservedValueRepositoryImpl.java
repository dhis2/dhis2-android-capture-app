package org.dhis2.usescases.reservedValue;


import com.squareup.sqlbrite2.BriteDatabase;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import java.util.List;
import io.reactivex.Observable;

public class ReservedValueRepositoryImpl implements ReservedValueRepository{

    private String SELECT_DATA_ELEMENTS = "SELECT te.uid, te.displayName,ou.uid, ou.displayName, count(rv.ownerUid) reservedValue " +
                    "FROM TrackedEntityAttribute te " +
                    "JOIN TrackedEntityAttributeReservedValue rv ON rv.ownerUid = te.uid " +
                    "JOIN organisationUnit ou ON ou.uid = rv.organisationUnit " +
                    "GROUP BY rv.organisationUnit, te.uid " +
                    "ORDER BY te.displayName";

    private final BriteDatabase briteDatabase;

    public ReservedValueRepositoryImpl(BriteDatabase briteDatabase){
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<List<ReservedValueModel>> getDataElements() {
        return briteDatabase.createQuery(TrackedEntityAttributeModel.TABLE, SELECT_DATA_ELEMENTS)
                .mapToList(cursor ->{
                    String organisationUnitUid = cursor.getString(2);
                    String organisationUnitName = cursor.getString(3);
                    String displayName = cursor.getString(1);
                    String reservedValue = cursor.getString(4);
                    String uid = cursor.getString(0);
                  return ReservedValueModel.create(uid,organisationUnitUid, organisationUnitName,displayName, reservedValue);
                });
    }
}
