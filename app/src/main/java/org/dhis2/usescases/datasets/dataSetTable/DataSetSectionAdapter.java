package org.dhis2.usescases.datasets.dataSetTable;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

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
    private boolean accessDataWrite;
    DataSetSectionAdapter(FragmentManager fm, boolean accessDataWrite) {
        super(fm);
        sectionArrays = new ArrayList<>();
        this.accessDataWrite = accessDataWrite;
    }

    @Override
    public Fragment getItem(int position) {
        return DataSetSectionFragment.create(sectionArrays.get(position), accessDataWrite);
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
