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

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public final class DataSetSectionAdapter extends FragmentStatePagerAdapter {

    private List<String> sections;
    private boolean accessDataWrite;
    private String dataSetUid;
    private Context context;
    private List<DataSetSectionFragment> fragments;

    DataSetSectionAdapter(FragmentManager fm, boolean accessDataWrite, String dataSetUid, Context context) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragments = new ArrayList<>();
        sections = new ArrayList<>();
        this.accessDataWrite = accessDataWrite;
        this.dataSetUid = dataSetUid;
        this.context = context;
    }

    public DataSetSectionFragment getCurrentItem(int position) {
        return fragments.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        DataSetSectionFragment fragment = DataSetSectionFragment.create(sections.get(position), accessDataWrite, dataSetUid);
        fragments.add(fragment);
        return fragment;
    }

    void swapData(List<String> sections) {
        this.sections = sections;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sections.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        SpannableString sb = new SpannableString(sections.get(position) + "  ");

        if (fragments.size() > position && (fragments.get(position).currentNumTables()  > 1)) {
            Drawable image = context.getResources().getDrawable(R.drawable.ic_arrow_down_white);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, sb.length() - 1, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }


}
