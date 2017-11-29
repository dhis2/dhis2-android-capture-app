package com.dhis2.usescases.teiDashboard.dashboardfragments;

import com.dhis2.usescases.general.FragmentGlobalAbstract;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class IndicatorsFragment extends FragmentGlobalAbstract {
    static IndicatorsFragment instance;

    static public IndicatorsFragment getInstance() {
        if (instance == null)
            instance = new IndicatorsFragment();

        return instance;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
