package com.dhis2.data.forms;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.hisp.dhis.android.dataentry.form.dataentry.DataEntryArguments;
import org.hisp.dhis.android.dataentry.form.dataentry.DataEntryFragment;

import java.util.ArrayList;
import java.util.List;

class FormSectionAdapter extends FragmentStatePagerAdapter {

    @NonNull
    private final List<FormSectionViewModel> formSectionViewModelList;

    FormSectionAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.formSectionViewModelList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        FormSectionViewModel viewModel = formSectionViewModelList.get(position);

        if (viewModel.type().equals(FormSectionViewModel.Type.ENROLLMENT)) {
            return DataEntryFragment.create(DataEntryArguments
                    .forEnrollment(viewModel.uid()));
        } else if (viewModel.type().equals(FormSectionViewModel.Type.PROGRAM_STAGE)) {
            return DataEntryFragment.create(DataEntryArguments
                    .forEvent(viewModel.uid()));
        } else if (viewModel.type().equals(FormSectionViewModel.Type.SECTION)) {
            return DataEntryFragment.create(DataEntryArguments
                    .forEventSection(viewModel.uid(), viewModel.sectionUid()));
        } else {
            throw new IllegalArgumentException("Unsupported page type");
        }
    }

    @Override
    public int getCount() {
        return formSectionViewModelList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return formSectionViewModelList.get(position).label();
    }

    void swapData(List<FormSectionViewModel> models) {
        formSectionViewModelList.clear();
        formSectionViewModelList.addAll(models);
        notifyDataSetChanged();
    }
}