package com.dhis2.usescases.teiDashboard.dashboardfragments;

import com.dhis2.usescases.general.FragmentGlobalAbstract;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class RelationshipFragment extends FragmentGlobalAbstract {
    static RelationshipFragment instance;

    static public RelationshipFragment getInstance() {
        if (instance == null)
            instance = new RelationshipFragment();

        return instance;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
