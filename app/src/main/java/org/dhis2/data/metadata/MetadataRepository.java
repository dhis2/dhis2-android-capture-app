package org.dhis2.data.metadata;

import org.hisp.dhis.android.core.program.ProgramModel;

import io.reactivex.Observable;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public interface MetadataRepository {

    /*EVENTS*/

    Observable<ProgramModel> getExpiryDateFromEvent(String eventUid);

}