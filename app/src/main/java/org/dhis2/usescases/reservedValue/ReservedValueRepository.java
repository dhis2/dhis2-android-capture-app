package org.dhis2.usescases.reservedValue;

import org.hisp.dhis.android.core.trackedentity.ReservedValueSummary;

import java.util.List;

import io.reactivex.Single;

public interface ReservedValueRepository {
    Single<List<ReservedValueSummary>> getReservedValues();
}
