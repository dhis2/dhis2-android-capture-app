package com.dhis2.usescases.appInfo;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

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

public class InfoRepositoryImpl implements InfoRepository {

    private final BriteDatabase briteDatabase;

    InfoRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> programs() {
        return briteDatabase.createQuery(ProgramModel.TABLE, "SELECT * FROM " + ProgramModel.TABLE)
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> events() {
        return briteDatabase.createQuery(EventModel.TABLE, "SELECT * FROM " + EventModel.TABLE)
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, "SELECT * FROM " + OrganisationUnitModel.TABLE)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances() {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, "SELECT * FROM " + TrackedEntityInstanceModel.TABLE)
                .mapToList(TrackedEntityInstanceModel::create);
    }

    @NonNull
    @Override
    public Observable<List<UserModel>> users() {
        return briteDatabase.createQuery(UserModel.TABLE, "SELECT * FROM " + UserModel.TABLE)
                .mapToList(UserModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryComboModel>> categoryCombo() {
        return briteDatabase.createQuery(CategoryComboModel.TABLE, "SELECT * FROM " + CategoryComboModel.TABLE)
                .mapToList(CategoryComboModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityTypeModel>> trackedEntitys() {
        return briteDatabase.createQuery(TrackedEntityTypeModel.TABLE, "SELECT * FROM " + TrackedEntityTypeModel.TABLE)
                .mapToList(TrackedEntityTypeModel::create);
    }

    @NonNull
    @Override
    public Observable<List<RelationshipTypeModel>> relationshipTypes() {
        return briteDatabase.createQuery(RelationshipTypeModel.TABLE, "SELECT * FROM " + RelationshipTypeModel.TABLE)
                .mapToList(RelationshipTypeModel::create);
    }

    @NonNull
    @Override
    public Observable<List<AuthenticatedUserModel>> authenticatedUsers() {
        return briteDatabase.createQuery(AuthenticatedUserModel.TABLE, "SELECT * FROM " + AuthenticatedUserModel.TABLE)
                .mapToList(AuthenticatedUserModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ConstantModel>> constants() {
        return briteDatabase.createQuery(ConstantModel.TABLE, "SELECT * FROM " + ConstantModel.TABLE)
                .mapToList(ConstantModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryModel>> categories() {
        return briteDatabase.createQuery(CategoryModel.TABLE, "SELECT * FROM " + CategoryModel.TABLE)
                .mapToList(CategoryModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> categoryOptionCombo() {
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, "SELECT * FROM " + CategoryOptionComboModel.TABLE)
                .mapToList(CategoryOptionComboModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionModel>> categoryOptions() {
        return briteDatabase.createQuery(CategoryOptionModel.TABLE, "SELECT * FROM " + CategoryOptionModel.TABLE)
                .mapToList(CategoryOptionModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentModel>> enrollments() {
        return briteDatabase.createQuery(EnrollmentModel.TABLE, "SELECT * FROM " + EnrollmentModel.TABLE)
                .mapToList(EnrollmentModel::create);
    }

    @NonNull
    @Override
    public Observable<List<RelationshipModel>> relationships() {
        return briteDatabase.createQuery(RelationshipModel.TABLE, "SELECT * FROM " + RelationshipModel.TABLE)
                .mapToList(RelationshipModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OptionSetModel>> optionSets() {
        return briteDatabase.createQuery(OptionSetModel.TABLE, "SELECT * FROM " + OptionSetModel.TABLE)
                .mapToList(OptionSetModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitProgramLinkModel>> organisationUnitProgramLinks() {
        return briteDatabase.createQuery(OrganisationUnitProgramLinkModel.TABLE, "SELECT * FROM " + OrganisationUnitProgramLinkModel.TABLE)
                .mapToList(OrganisationUnitProgramLinkModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OptionModel>> options() {
        return briteDatabase.createQuery(OptionModel.TABLE, "SELECT * FROM " + OptionModel.TABLE)
                .mapToList(OptionModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityAttributeModel>> trackedEntityAttributes() {
        return briteDatabase.createQuery(TrackedEntityAttributeModel.TABLE, "SELECT * FROM " + TrackedEntityAttributeModel.TABLE)
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @NonNull
    @Override
    public Observable<List<DataElementModel>> dataElements() {
        return briteDatabase.createQuery(DataElementModel.TABLE, "SELECT * FROM " + DataElementModel.TABLE)
                .mapToList(DataElementModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramRuleModel>> programRule() {
        return briteDatabase.createQuery(ProgramRuleModel.TABLE, "SELECT * FROM " + ProgramRuleModel.TABLE)
                .mapToList(ProgramRuleModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageSectionModel>> programStageSections() {
        return briteDatabase.createQuery(ProgramStageSectionModel.TABLE, "SELECT * FROM " + ProgramStageSectionModel.TABLE)
                .mapToList(ProgramStageSectionModel::create);
    }



    @NonNull
    @Override
    public Observable<List<UserRoleModel>> userRole() {
        return briteDatabase.createQuery(UserRoleModel.TABLE, "SELECT * FROM " + UserRoleModel.TABLE)
                .mapToList(UserRoleModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageModel>> programStages() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, "SELECT * FROM " + ProgramStageModel.TABLE)
                .mapToList(ProgramStageModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramIndicatorModel>> programIndicator() {
        return briteDatabase.createQuery(ProgramIndicatorModel.TABLE, "SELECT * FROM " + ProgramIndicatorModel.TABLE)
                .mapToList(ProgramIndicatorModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryCategoryComboLinkModel>> categoryCategoryComboLink() {
        return briteDatabase.createQuery(CategoryCategoryComboLinkModel.TABLE, "SELECT * FROM " + CategoryCategoryComboLinkModel.TABLE)
                .mapToList(CategoryCategoryComboLinkModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboCategoryLinkModel>> categoryOptionComboCategoryLink() {
        return briteDatabase.createQuery(CategoryOptionComboCategoryLinkModel.TABLE, "SELECT * FROM " + CategoryOptionComboCategoryLinkModel.TABLE)
                .mapToList(CategoryOptionComboCategoryLinkModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryCategoryOptionLinkModel>> categoryCategoryOptionLink() {
        return briteDatabase.createQuery(CategoryCategoryOptionLinkModel.TABLE, "SELECT * FROM " + CategoryCategoryOptionLinkModel.TABLE)
                .mapToList(CategoryCategoryOptionLinkModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> trackedEntityAttributeValue() {
        return briteDatabase.createQuery(TrackedEntityAttributeValueModel.TABLE, "SELECT * FROM " + TrackedEntityAttributeValueModel.TABLE)
                .mapToList(TrackedEntityAttributeValueModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityDataValueModel>> trackedEntityDataValue() {
        return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, "SELECT * FROM " + TrackedEntityDataValueModel.TABLE)
                .mapToList(TrackedEntityDataValueModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramTrackedEntityAttributeModel>> programTrackedEntityAttribute() {
        return briteDatabase.createQuery(ProgramTrackedEntityAttributeModel.TABLE, "SELECT * FROM " + ProgramTrackedEntityAttributeModel.TABLE)
                .mapToList(ProgramTrackedEntityAttributeModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramRuleVariableModel>> programRuleVariable() {
        return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE, "SELECT * FROM " + ProgramRuleVariableModel.TABLE)
                .mapToList(ProgramRuleVariableModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramRuleActionModel>> programRuleAction() {
        return briteDatabase.createQuery(ProgramRuleActionModel.TABLE, "SELECT * FROM " + ProgramRuleActionModel.TABLE)
                .mapToList(ProgramRuleActionModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageDataElementModel>> programStageDataElement() {
        return briteDatabase.createQuery(ProgramStageDataElementModel.TABLE, "SELECT * FROM " + ProgramStageDataElementModel.TABLE)
                .mapToList(ProgramStageDataElementModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageSectionProgramIndicatorLinkModel>> programStageSectionProgramIndicatorLink() {
        return briteDatabase.createQuery(ProgramStageSectionProgramIndicatorLinkModel.TABLE, "SELECT * FROM " + ProgramStageSectionProgramIndicatorLinkModel.TABLE)
                .mapToList(ProgramStageSectionProgramIndicatorLinkModel::create);
    }

    @NonNull
    @Override
    public Observable<List<SystemInfoModel>> systemInfo() {
        return briteDatabase.createQuery(SystemInfoModel.TABLE, "SELECT * FROM " + SystemInfoModel.TABLE)
                .mapToList(SystemInfoModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ResourceModel>> resources() {
        return briteDatabase.createQuery(ResourceModel.TABLE, "SELECT * FROM " + ResourceModel.TABLE)
                .mapToList(ResourceModel::create);
    }


}
