package org.dhis2.usescases.reservedValue;

import java.util.List;

import io.reactivex.Observable;

public interface ReservedValueRepository {

    Observable<List<ReservedValueModel>> getDataElements();
}
