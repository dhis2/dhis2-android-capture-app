package org.dhis2.data.metadata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public interface MetadataRepository {

    /*PROGRAMS*/
    Observable<List<ProgramModel>> getTeiActivePrograms(String teiUid, boolean showOnlyActive);

    Observable<ProgramModel> getProgramWithId(String programUid);

    /*TRACKED ENTITY*/

    Observable<TrackedEntityTypeModel> getTrackedEntity(String trackedEntityUid);


    /*CATEGORY OPTION*/

    Observable<String> getDefaultCategoryOptionId();


    /*CATEGORY OPTION COMBO*/


    void saveCatOption(String eventUid, String catOptionComboUid);

    Observable<String> getDefaultCategoryOptionComboId();


    /*ORG UNIT*/

    Observable<OrganisationUnitModel> getOrganisationUnit(String orgUnitUid);

    Observable<List<OrganisationUnitModel>> getTeiOrgUnits(String teiUid);

    Observable<List<OrganisationUnitModel>> getTeiOrgUnits(@NonNull String teiUid, @Nullable String programUid);


    //ProgramStage

    @NonNull
    Observable<ProgramStageModel> programStage(String programStageId);

    /*ENROLLMENTS*/
    Observable<List<EnrollmentModel>> getTEIEnrollments(String teiUid);


    /*EVENTS*/

    Observable<ProgramModel> getExpiryDateFromEvent(String eventUid);

    Observable<Boolean> isCompletedEventExpired(String eventUid);


    /*OPTION SET*/
    List<OptionModel> optionSet(String optionSetId);

    /*RESOURCE*/

    /*SETINGS*/
    Observable<Pair<String, Integer>> getTheme();

    Observable<ObjectStyleModel> getObjectStyle(String uid);

    Observable<List<OrganisationUnitModel>> getOrganisationUnits();

    Flowable<Pair<Integer, Integer>> getDownloadedData();

    Observable<String> getServerUrl();

    List<TrackerImportConflict> getSyncErrors();

    Observable<List<OptionModel>> searchOptions(String text, String idOptionSet, int page, List<String> optionsToHide, List<String> optionsGroupsToHide);

    Observable<Map<String, ObjectStyleModel>> getObjectStylesForPrograms(List<ProgramModel> enrollmentProgramModels);

    Flowable<ProgramStageModel> programStageForEvent(String eventId);
}