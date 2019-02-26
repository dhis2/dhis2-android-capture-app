package org.dhis2.data.forms;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.Log;

import org.dhis2.data.forms.dataentry.DataEntryArguments;
import org.dhis2.data.forms.dataentry.DataEntryFragment;

import java.util.ArrayList;
import java.util.List;

public class FormSectionAdapter extends FragmentStatePagerAdapter {

    @NonNull
    private final List<FormSectionViewModel> formSectionViewModelList;
    private final List<String> sections;
    private final FragmentManager fm;

    FormSectionAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.formSectionViewModelList = new ArrayList<>();
        this.sections = new ArrayList<>();
        this.fm = fragmentManager;
    }

    @Override
    public Fragment getItem(int position) {
        FormSectionViewModel viewModel = formSectionViewModelList.get(position);

        if (viewModel.type().equals(FormSectionViewModel.Type.ENROLLMENT)) {
            return DataEntryFragment.create(DataEntryArguments
                    .forEnrollment(viewModel.uid()));
        } else if (viewModel.type().equals(FormSectionViewModel.Type.PROGRAM_STAGE)) {
            return DataEntryFragment.create(DataEntryArguments
                    .forEvent(viewModel.uid(),viewModel.renderType()));
        } else if (viewModel.type().equals(FormSectionViewModel.Type.SECTION)) {
            return DataEntryFragment.create(DataEntryArguments
                    .forEventSection(viewModel.uid(), viewModel.sectionUid(), viewModel.renderType()));
        } else {
            throw new IllegalArgumentException("Unsupported page type");
        }
    }

    @Override
    public int getCount() {
        return sections.isEmpty() ? formSectionViewModelList.size() : sections.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return formSectionViewModelList.get(position).label();
    }

    @Override
    public int getItemPosition(@NonNull Object item) {
        DataEntryFragment fragment = (DataEntryFragment) item;
        String section = fragment.getSection();
        int position = sections.indexOf(section);

        return position >= 0 ? position : POSITION_NONE;
    }


    void swapData(List<FormSectionViewModel> models) {

        List<String> newSections = new ArrayList<>();
        boolean differentSections = false;

        for (int i = 0; i < models.size(); i++) {
            FormSectionViewModel item = models.get(i);
            newSections.add(item.sectionUid());
        }

        if (sections.size() == models.size()) //If previous sections size = new sections size we check if each section is the same
            for (String section : newSections) {
                if (section!=null && !section.equals(sections.get(0)))
                    differentSections = true;
            }
        else
            differentSections = true;

        if (differentSections || sections.isEmpty()) {

            Log.d("FM_TEST", fm.getFragments().size() + " ");

            formSectionViewModelList.clear();
            sections.clear();
            formSectionViewModelList.addAll(models);
            sections.addAll(newSections);
            notifyDataSetChanged();
        }
    }

    public boolean areDifferentSections(List<FormSectionViewModel> models) {
        List<String> newSections = new ArrayList<>();
        boolean differentSections = false;

        for (int i = 0; i < models.size(); i++) {
            FormSectionViewModel item = models.get(i);
            if (item.sectionUid() != null)
                newSections.add(item.sectionUid());
        }

        if (sections.size() == models.size()) //If previous sections size = new sections size we check if each section is the same
            for (String section : newSections) {
                if (!section.equals(sections.get(newSections.indexOf(section))))
                    differentSections = true;
            }
        else
            differentSections = true;

        return differentSections;
    }
}