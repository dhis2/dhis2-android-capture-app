package org.dhis2.usescases.datasets.dataSetTable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailFragment;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;

import java.util.ArrayList;
import java.util.List;

public final class DataSetSectionAdapter extends FragmentStateAdapter {

    private List<String> sections;
    private boolean accessDataWrite;
    private String dataSetUid;

    DataSetSectionAdapter(FragmentActivity fragmentActivity, boolean accessDataWrite, String dataSetUid) {
        super(fragmentActivity);
        sections = new ArrayList<>();
        this.accessDataWrite = accessDataWrite;
        this.dataSetUid = dataSetUid;
    }

    void swapData(List<String> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        //TODO (ANDROAPP-2588): SUPPORT FOR NOTES FRAGMENT
        if (position == 0) {
            fragment = DataSetDetailFragment.create(dataSetUid, accessDataWrite);
        } else {
            fragment = DataSetSectionFragment.create(sections.get(position - 1), accessDataWrite, dataSetUid);
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        //TODO (ANDROAPP-2588): SUPPORT FOR NOTES FRAGMENT
        return sections.size() + 1;
    }

    String getSectionTitle(int position) {
        if (sections != null && sections.size() > position - 1) {
            return sections.get(position - 1);
        } else {
            return "";
        }
    }
}
