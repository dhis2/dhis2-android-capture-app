package org.dhis2.data.metadata;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public interface MetadataRepository {

    /*EVENTS*/

    Observable<ProgramModel> getExpiryDateFromEvent(String eventUid);

    Observable<Boolean> isCompletedEventExpired(String eventUid);


    /*SETINGS*/
    Observable<Pair<String, Integer>> getTheme();



    Flowable<Pair<Integer, Integer>> getDownloadedData();

    Observable<String> getServerUrl();

    List<TrackerImportConflict> getSyncErrors();

    Observable<List<OptionModel>> searchOptions(String text, String idOptionSet, int page, List<String> optionsToHide, List<String> optionsGroupsToHide);




}