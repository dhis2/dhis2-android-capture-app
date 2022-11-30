package org.dhis2.usescases.datasets.dataSetTable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dhis2.R;
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailFragment;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;

import java.util.ArrayList;
import java.util.List;

public final class DataSetSectionAdapter extends FragmentStateAdapter {

    private List<DataSetSection> sections;
    private final boolean accessDataWrite;
    private final String dataSetUid;
    private final String orgUnitUid;
    private final String periodId;
    private final String attrOptComboUid;

    DataSetSectionAdapter(FragmentActivity fragmentActivity,
                          boolean accessDataWrite,
                          String dataSetUid,
                          String orgUnitUid,
                          String periodId,
                          String attrOptionComboUid) {
        super(fragmentActivity);
        sections = new ArrayList<>();
        this.accessDataWrite = accessDataWrite;
        this.dataSetUid = dataSetUid;
        this.orgUnitUid = orgUnitUid;
        this.periodId = periodId;
        this.attrOptComboUid = attrOptionComboUid;
    }

    void swapData(List<DataSetSection> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = DataSetDetailFragment.create(dataSetUid, accessDataWrite);
        } else {
            fragment = DataSetSectionFragment.create(
                    sections.get(position - 1).getUid(),
                    accessDataWrite,
                    dataSetUid,
                    orgUnitUid,
                    periodId,
                    attrOptComboUid);
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return sections.size() + 1;
    }

    String getSectionTitle(int position) {
        if (sections != null && sections.size() > position - 1) {
            return sections.get(position - 1).title();
        } else {
            return "";
        }
    }

    public int getNavigationPagePosition(int itemId) {
        switch (itemId) {
            case R.id.navigation_details:
                return 0;
            case R.id.navigation_data_entry:
                return 1;
            default:
                return 2;
        }
    }
}
