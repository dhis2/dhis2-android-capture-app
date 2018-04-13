package com.dhis2.usescases.appInfo;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.category.CategoryCategoryComboLinkModel;
import org.hisp.dhis.android.core.category.CategoryCategoryOptionLinkModel;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboCategoryLinkModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.constant.ConstantModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.option.OptionSetModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramRuleActionModel;
import org.hisp.dhis.android.core.program.ProgramRuleModel;
import org.hisp.dhis.android.core.program.ProgramRuleVariableModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionProgramIndicatorLinkModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.systeminfo.SystemInfoModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;
import org.hisp.dhis.android.core.user.AuthenticatedUserModel;
import org.hisp.dhis.android.core.user.UserModel;
import org.hisp.dhis.android.core.user.UserRoleModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public interface InfoRepository {

    @NonNull
    Observable<List<ProgramModel>> programs();

    @NonNull
    Observable<List<EventModel>> events();

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances();

    @NonNull
    Observable<List<UserModel>> users();

    @NonNull
    Observable<List<CategoryComboModel>> categoryCombo();

    @NonNull
    Observable<List<TrackedEntityTypeModel>> trackedEntitys();

    @NonNull
    Observable<List<RelationshipTypeModel>> relationshipTypes();

    @NonNull
    Observable<List<AuthenticatedUserModel>> authenticatedUsers();

    @NonNull
    Observable<List<ConstantModel>> constants();

    @NonNull
    Observable<List<CategoryModel>> categories();

    @NonNull
    Observable<List<CategoryOptionComboModel>> categoryOptionCombo();

    @NonNull
    Observable<List<CategoryOptionModel>> categoryOptions();

    @NonNull
    Observable<List<EnrollmentModel>> enrollments();

    @NonNull
    Observable<List<RelationshipModel>> relationships();

    @NonNull
    Observable<List<OptionSetModel>> optionSets();

    @NonNull
    Observable<List<OrganisationUnitProgramLinkModel>> organisationUnitProgramLinks();

    @NonNull
    Observable<List<OptionModel>> options();

    @NonNull
    Observable<List<TrackedEntityAttributeModel>> trackedEntityAttributes();

    @NonNull
    Observable<List<DataElementModel>> dataElements();

    @NonNull
    Observable<List<ProgramRuleModel>> programRule();

    @NonNull
    Observable<List<ProgramStageSectionModel>> programStageSections();

    @NonNull
    Observable<List<UserRoleModel>> userRole();

    @NonNull
    Observable<List<ProgramStageModel>> programStages();

    @NonNull
    Observable<List<ProgramIndicatorModel>> programIndicator();

    @NonNull
    Observable<List<CategoryCategoryComboLinkModel>> categoryCategoryComboLink();

    @NonNull
    Observable<List<CategoryOptionComboCategoryLinkModel>> categoryOptionComboCategoryLink();

    @NonNull
    Observable<List<CategoryCategoryOptionLinkModel>> categoryCategoryOptionLink();

    @NonNull
    Observable<List<TrackedEntityAttributeValueModel>> trackedEntityAttributeValue();

    @NonNull
    Observable<List<TrackedEntityDataValueModel>> trackedEntityDataValue();

    @NonNull
    Observable<List<ProgramTrackedEntityAttributeModel>> programTrackedEntityAttribute();

    @NonNull
    Observable<List<ProgramRuleVariableModel>> programRuleVariable();

    @NonNull
    Observable<List<ProgramRuleActionModel>> programRuleAction();

    @NonNull
    Observable<List<ProgramStageDataElementModel>> programStageDataElement();

    @NonNull
    Observable<List<ProgramStageSectionProgramIndicatorLinkModel>> programStageSectionProgramIndicatorLink();

    @NonNull
    Observable<List<SystemInfoModel>> systemInfo();

    @NonNull
    Observable<List<ResourceModel>> resources();


}
