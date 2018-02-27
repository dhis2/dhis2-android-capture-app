package com.dhis2.data.metadata;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.List;

import io.reactivex.Observable;


/**
 * Created by ppajuelo on 04/12/2017.
 *
 */

public interface MetadataRepository {

    /*PROGRAMS*/
    Observable<List<ProgramModel>> getProgramModelFromEnrollmentList(List<Enrollment> enrollments);

    Observable<ProgramModel> getProgramWithId(String programUid);

    /*TRACKED ENTITY*/

    Observable<TrackedEntityModel> getTrackedEntity(String trackedEntityUid);

    /*CATEGORY OPTION*/

    Observable<CategoryOptionModel> getCategoryOptionWithId(String categoryOptionId);

    /*CATEGORY OPTION COMBO*/

    Observable<CategoryOptionComboModel> getCategoryOptionComboWithId(String categoryOptionComboId);

    /*CATEGORY COMBO*/

    Observable<CategoryComboModel> getCategoryComboWithId(String categoryComboId);

    /*ORG UNIT*/

    Observable<OrganisationUnitModel> getOrganisationUnit(String orgUnitUid);

    /*PROGRAM TRACKED ENTITY ATTRIBUTE*/

    Observable<List<ProgramTrackedEntityAttributeModel>> getProgramTrackedEntityAttributes(String programUid);

    /*RELATIONSHIPS*/

    Observable<RelationshipTypeModel> getRelationshipType(String programUid);

    Observable<List<RelationshipTypeModel>> getRelationshipTypeList();

    //ProgramStage

    @NonNull
    Observable<ProgramStageModel> programStage(String programStageId);

    Observable<DataElementModel> getDataElement(String dataElementUid);

    Observable<Integer> getProgramStageDataElementCount(String programStageId);

    Observable<Integer> getTrackEntityDataValueCount(String programStageId);
}
