package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import com.squareup.sqlbrite2.BriteDatabase;

/**
 * Created by ppajuelo on 02/11/2017.
 *
 */

public class EventInfoSectionsRepositoryImpl implements EventInfoSectionsRepository {

    private final BriteDatabase briteDatabase;

    EventInfoSectionsRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

}