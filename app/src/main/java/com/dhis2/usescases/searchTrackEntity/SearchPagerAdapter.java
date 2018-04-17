package com.dhis2.usescases.searchTrackEntity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * QUADRAM. Created by ppajuelo on 16/04/2018.
 */

public class SearchPagerAdapter extends FragmentStatePagerAdapter {

    private int maxData = 2;

    public SearchPagerAdapter(FragmentManager fm) {
        super(fm);
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
        return position == 0 ? "Local Results" : "Online Results";
    }

    void setOnline(Boolean online) {
        this.maxData = online ? 2 : 1;
    }
}
