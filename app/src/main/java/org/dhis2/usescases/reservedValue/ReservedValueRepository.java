package org.dhis2.usescases.reservedValue;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface ReservedValueRepository {

    Flowable<List<ReservedValueModel>> getDataElements();
}
