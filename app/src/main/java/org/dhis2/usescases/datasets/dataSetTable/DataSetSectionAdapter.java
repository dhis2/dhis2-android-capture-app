package org.dhis2.usescases.datasets.dataSetTable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;
import org.hisp.dhis.android.core.dataelement.DataElementModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public final class DataSetSectionAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> sectionArrays;

    DataSetSectionAdapter(FragmentManager fm) {
        super(fm);
        sectionArrays = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return DataSetSectionFragment.create(sectionArrays.get(position));
    }

    void swapData(Map<String, List<DataElementModel>> dataElements) {
        sectionArrays = new ArrayList<>(dataElements.keySet());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sectionArrays.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return sectionArrays.get(position);
    }
}
