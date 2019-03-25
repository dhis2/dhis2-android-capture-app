package org.dhis2.usescases.reservedValue;

import java.util.List;

import io.reactivex.Flowable;

public interface ReservedValueRepository {

    Flowable<List<ReservedValueModel>> getDataElements();
}
