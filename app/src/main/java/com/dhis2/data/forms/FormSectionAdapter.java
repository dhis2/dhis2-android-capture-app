package com.dhis2.data.forms;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dhis2.data.forms.dataentry.DataEntryArguments;
import com.dhis2.data.forms.dataentry.DataEntryFragment;
import com.dhis2.utils.CustomFragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FormSectionAdapter extends CustomFragmentStatePagerAdapter {

    @NonNull
    private final List<FormSectionViewModel> formSectionViewModelList;
    //    private final List<DataEntryFragment> dataEntryFragments;
    private final List<String> sections;

    FormSectionAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.formSectionViewModelList = new ArrayList<>();
//        this.dataEntryFragments = new ArrayList<>();
        this.sections = new ArrayList<>();
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
                    .forEventSection(viewModel.uid(), viewModel.sectionUid(), viewModel.renderType()));
        } else {
            throw new IllegalArgumentException("Unsupported page type");
        }
//        return dataEntryFragments.get(position);
    }

    @Override
    public int getCount() {
        return formSectionViewModelList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return formSectionViewModelList.get(position).label();
    }

    public int getItemPosition(@NonNull Object item) {
        DataEntryFragment fragment = (DataEntryFragment) item;
        String section = fragment.getSection();
        int position = sections.indexOf(section);

        return position >= 0 ? position : POSITION_NONE;

    }


    void swapData(List<FormSectionViewModel> models) {
        formSectionViewModelList.clear();
//        dataEntryFragments.clear();
        sections.clear();
        formSectionViewModelList.addAll(models);
        for (FormSectionViewModel viewModel : models) {
            sections.add(viewModel.sectionUid());
//            if (viewModel.type().equals(FormSectionViewModel.Type.ENROLLMENT)) {
//                dataEntryFragments.add(DataEntryFragment.create(DataEntryArguments
//                        .forEnrollment(viewModel.uid())));
//            } else if (viewModel.type().equals(FormSectionViewModel.Type.PROGRAM_STAGE)) {
//                dataEntryFragments.add(DataEntryFragment.create(DataEntryArguments
//                        .forEvent(viewModel.uid())));
//            } else if (viewModel.type().equals(FormSectionViewModel.Type.SECTION)) {
//                dataEntryFragments.add(DataEntryFragment.create(DataEntryArguments
//                        .forEventSection(viewModel.uid(), viewModel.sectionUid(), viewModel.renderType())));
//            }
        }
        notifyDataSetChanged();
    }

    @Override
    public String getTag(int position) {
        return sections.get(position);
    }
}