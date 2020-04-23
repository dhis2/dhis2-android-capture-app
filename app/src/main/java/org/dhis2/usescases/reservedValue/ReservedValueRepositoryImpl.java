package org.dhis2.usescases.reservedValue;


import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.trackedentity.ReservedValueSummary;

import java.util.List;

import io.reactivex.Single;

public class ReservedValueRepositoryImpl implements ReservedValueRepository {

    private final D2 d2;

    public ReservedValueRepositoryImpl(D2 d2) {
        this.d2 = d2;
    }

    @Override
    public Single<List<ReservedValueSummary>> getReservedValues() {
        return d2.trackedEntityModule().reservedValueManager().getReservedValueSummaries();
    }
}
