package org.dhis2.data.server;

import io.reactivex.Flowable;

public interface DataBaseExporter {
    Flowable<Boolean> exportDb();
    Flowable<Boolean> importDb();
}
