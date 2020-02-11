package org.dhis2.usescases.datasets.dataSetTable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dhis2.R;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public final class DataSetSectionAdapter extends FragmentStateAdapter {

    private List<String> sections;
    private boolean accessDataWrite;
    private String dataSetUid;
    private List<DataSetSectionFragment> fragments;

    DataSetSectionAdapter(FragmentActivity activity, boolean accessDataWrite, String dataSetUid) {
        super(activity);
        fragments = new ArrayList<>();
        sections = new ArrayList<>();
        this.accessDataWrite = accessDataWrite;
        this.dataSetUid = dataSetUid;
    }

    public boolean hasFragmentAt(int position){
        return position < fragments.size();
    }

    public DataSetSectionFragment getCurrentItem(int position) {
        return fragments.get(position);
    }

    @Override
    public Fragment createFragment(int position) {
        DataSetSectionFragment fragment = DataSetSectionFragment.create(sections.get(position), accessDataWrite, dataSetUid);
        fragments.add(fragment);
        return fragment;
    }

    void swapData(List<String> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }
}
