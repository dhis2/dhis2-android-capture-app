package org.dhis2.usescases.datasets.dataSetTable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.dhis2.R;
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
    private String dataSetUid;
    private Context context;
    private List<DataSetSectionFragment> fragments;
    DataSetSectionAdapter(FragmentManager fm, boolean accessDataWrite, String dataSetUid, Context context) {
        super(fm);
        fragments = new ArrayList<>();
        sectionArrays = new ArrayList<>();
        this.accessDataWrite = accessDataWrite;
        this.dataSetUid = dataSetUid;
        this.context = context;
    }

    public DataSetSectionFragment getCurrentItem(int position){
        return fragments.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        DataSetSectionFragment fragment = DataSetSectionFragment.create(sectionArrays.get(position), accessDataWrite, dataSetUid);
        fragments.add(fragment);
        return fragment;
    }

    void swapData(Map<String, List<DataElementModel>> dataElements) {
        sectionArrays = new ArrayList<>(dataElements.keySet());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sectionArrays.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        SpannableString sb = new SpannableString(sectionArrays.get(position) + "  ");

        if(fragments.size()>position && fragments.get(position).currentNumTables()>1) {
            Drawable image = context.getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, sb.length() - 1, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }


}
