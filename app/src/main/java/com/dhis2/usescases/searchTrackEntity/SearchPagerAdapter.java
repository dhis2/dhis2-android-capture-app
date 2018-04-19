package com.dhis2.usescases.searchTrackEntity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dhis2.R;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

/**
 * QUADRAM. Created by ppajuelo on 16/04/2018.
 */

public class SearchPagerAdapter extends FragmentStatePagerAdapter {

    private final ActivityGlobalAbstract context;
    private int maxData = 2;

    public SearchPagerAdapter(ActivityGlobalAbstract context) {
        super(context.getSupportFragmentManager());
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return position == 0 ? SearchLocalFragment.getInstance() : SearchOnlineFragment.getInstance();
    }

    @Override
    public int getCount() {
        return maxData;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? context.getString(R.string.local_results) :
                context.getString(R.string.online_results);
    }

    void setOnline(Boolean online) {
        this.maxData = online ? 2 : 1;
    }
}
