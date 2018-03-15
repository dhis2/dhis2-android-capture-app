package com.dhis2.usescases.appInfo;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.data.service.SyncService;
import com.dhis2.databinding.FragmentInfoBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 31/01/2018.
 *
 */

public class AppInfoFragment extends FragmentGlobalAbstract {

    FragmentInfoBinding binding;
    CompositeDisposable compositeDisposable;
    @Inject
    InfoRepository infoRepository;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Components) getActivity().getApplicationContext()).userComponent()
                .plus(new InfoModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, container, false);
        compositeDisposable = new CompositeDisposable();
        binding.buttonSync.setOnClickListener(view -> view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncService.class)));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {

        compositeDisposable.add(infoRepository.programs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programs.setText("Programs: " + data.size()))
        );

        compositeDisposable.add(infoRepository.events()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.events.setText("Events: " + data.size()))
        );

        compositeDisposable.add(infoRepository.orgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.orgUnit.setText("OrgUnits: " + data.size()))
        );

        compositeDisposable.add(infoRepository.trackedEntityInstances()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.tei.setText("TEI: " + data.size()))
        );

        compositeDisposable.add(infoRepository.users()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.users.setText("Users: " + data.size()))
        );

        compositeDisposable.add(infoRepository.categoryCombo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.categoryCombo.setText("CategoryCombo: " + data.size()))
        );

        compositeDisposable.add(infoRepository.trackedEntitys()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.trackedEntitys.setText("TrackedEntitys: " + data.size()))
        );

        compositeDisposable.add(infoRepository.relationshipTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.relationshipTypes.setText("RelationshipTypes: " + data.size()))
        );

        compositeDisposable.add(infoRepository.authenticatedUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.authenticatedUsers.setText("AuthenticatedUsers: " + data.size()))
        );

        compositeDisposable.add(infoRepository.constants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.constants.setText("Constants: " + data.size()))
        );

        compositeDisposable.add(infoRepository.categories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.categories.setText("Categories: " + data.size()))
        );

        compositeDisposable.add(infoRepository.categoryOptionCombo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.categoryOptionCombo.setText("CategoryOptionCombo: " + data.size()))
        );

        compositeDisposable.add(infoRepository.categoryOptions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.categoryOptions.setText("CategoryOptions: " + data.size()))
        );

        compositeDisposable.add(infoRepository.enrollments()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.enrollments.setText("Enrollments: " + data.size()))
        );

        compositeDisposable.add(infoRepository.relationships()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.relationships.setText("Relationships: " + data.size()))
        );

        compositeDisposable.add(infoRepository.optionSets()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.optionSets.setText("OptionSets: " + data.size()))
        );

        compositeDisposable.add(infoRepository.organisationUnitProgramLinks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.organisationUnitProgramLinks.setText("OrganisationUnitProgramLinks: " + data.size()))
        );

        compositeDisposable.add(infoRepository.options()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.options.setText("Options: " + data.size()))
        );

        compositeDisposable.add(infoRepository.trackedEntityAttributes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.trackedEntityAttributes.setText("TrackedEntityAttributes: " + data.size()))
        );

        compositeDisposable.add(infoRepository.dataElements()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.dataElements.setText("DataElements: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programRule()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programRule.setText("ProgramRule: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programStageSections()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programStageSections.setText("ProgramStageSections: " + data.size()))
        );

        compositeDisposable.add(infoRepository.userRoleProgramLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.userRoleProgramLink.setText("UserRoleProgramLink: " + data.size()))
        );

        compositeDisposable.add(infoRepository.userRole()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.userRole.setText("UserRole: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programStages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programStages.setText("ProgramStages: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programIndicator()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programIndicator.setText("ProgramIndicator: " + data.size()))
        );

        compositeDisposable.add(infoRepository.categoryCategoryComboLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.categoryCategoryComboLink.setText("CategoryCategoryComboLink: " + data.size()))
        );

        compositeDisposable.add(infoRepository.categoryOptionComboCategoryLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.categoryOptionComboCategoryLink.setText("CategoryOptionComboCategoryLink: " + data.size()))
        );

        compositeDisposable.add(infoRepository.categoryCategoryOptionLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.categoryCategoryOptionLink.setText("CategoryCategoryOptionLink: " + data.size()))
        );

        compositeDisposable.add(infoRepository.trackedEntityAttributeValue()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.trackedEntityAttributeValue.setText("TrackedEntityAttributeValue: " + data.size()))
        );

        compositeDisposable.add(infoRepository.trackedEntityDataValue()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.trackedEntityDataValue.setText("TrackedEntityDataValue: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programTrackedEntityAttribute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programTrackedEntityAttribute.setText("ProgramTrackedEntityAttribute: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programRuleVariable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programRuleVariable.setText("ProgramRuleVariable: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programRuleAction()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programRuleAction.setText("ProgramRuleAction: " + data.size()))
        );

        compositeDisposable.add(infoRepository.programStageDataElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programStageDataElement.setText("ProgramStageDataElement: " + data.size()))
        );


        compositeDisposable.add(infoRepository.programStageSectionProgramIndicatorLink()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programStageSectionProgramIndicatorLink.setText("ProgramStageSectionProgramIndicatorLink: " + data.size()))
        );

        compositeDisposable.add(infoRepository.systemInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.systemInfo.setText("SystemInfo: " + data.size()))
        );

        compositeDisposable.add(infoRepository.resources()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.resources.setText("Resources: " + data.size()))
        );


    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }


}
