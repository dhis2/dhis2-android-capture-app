package com.dhis2.usescases.teiDashboard.dashboardfragments;

import com.dhis2.usescases.general.FragmentGlobalAbstract;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract {

    static TEIDataFragment instance;

    static public TEIDataFragment getInstance() {
        if (instance == null)
            instance = new TEIDataFragment();

        return instance;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
