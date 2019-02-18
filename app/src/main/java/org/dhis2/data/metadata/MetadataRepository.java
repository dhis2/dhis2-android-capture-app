package org.dhis2.data.metadata;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Flowable;
import io.reactivex.Observable;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public interface MetadataRepository {

    /*PROGRAMS*/
    Observable<List<ProgramModel>> getTeiActivePrograms(String teiUid);

    Observable<ProgramModel> getProgramWithId(String programUid);

    /*TRACKED ENTITY*/

    Observable<TrackedEntityTypeModel> getTrackedEntity(String trackedEntityUid);

    Observable<TrackedEntityInstanceModel> getTrackedEntityInstance(String teiUid);

    /*CATEGORY OPTION*/

    Observable<String> getDefaultCategoryOptionId();


    /*CATEGORY OPTION COMBO*/

    Observable<CategoryOptionComboModel> getCategoryOptionComboWithId(String categoryOptionComboId);

    Observable<List<CategoryOptionComboModel>> getCategoryComboOptions(String categoryComboId);

    void saveCatOption(String eventUid, CategoryOptionComboModel selectedOption);

    /*CATEGORY COMBO*/

    Observable<CategoryComboModel> getCategoryComboWithId(String categoryComboId);

    /*ORG UNIT*/

    Observable<OrganisationUnitModel> getOrganisationUnit(String orgUnitUid);

    Observable<OrganisationUnitModel> getTeiOrgUnit(String teiUid);

    Observable<OrganisationUnitModel> getTeiOrgUnit(@NonNull String teiUid, @Nullable String programUid);

    /*PROGRAM TRACKED ENTITY ATTRIBUTE*/

    Observable<List<ProgramTrackedEntityAttributeModel>> getProgramTrackedEntityAttributes(String programUid);


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


    @NonNull
    Observable<List<ResourceModel>> syncState(ProgramModel program);

    Flowable<Pair<Integer, Integer>> getDownloadedData();

    Observable<String> getServerUrl();

    Observable<List<D2Error>> getSyncErrors();

    Observable<List<OptionModel>> searchOptions(String text, String idOptionSet, int page);

    int optionSetSize(String optionSet);
}