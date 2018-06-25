package com.dhis2.data.forms;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.DataEntryArguments;
import com.dhis2.data.forms.dataentry.DataEntryFragment;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
                    .forEvent(viewModel.uid()));
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
                if (!section.equals(sections.get(0)))
                    differentSections = true;
            }
        else
            differentSections = true;

        /*if(differentSections){
            for(Fragment fragment : fm.getFragments()){
                fm.beginTransaction().remove(fragment).commit();
            }
        }*/

        if (differentSections || sections.isEmpty()) {

            Log.d("FM_TEST", fm.getFragments().size() + " ");

            formSectionViewModelList.clear();
            sections.clear();
            formSectionViewModelList.addAll(models);
            sections.addAll(newSections);
            notifyDataSetChanged();

        }
    }

   /* @Override
    public String getTag(int position) {
        return sections.isEmpty()?"section":sections.get(position);
    }*/

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            super.destroyItem(container, position, object);
        } catch (IllegalStateException e) {
            Timber.e(e);
        }
    }
}