package com.dhis2.usescases.teiDashboard.dashboardfragments;

import com.dhis2.usescases.general.FragmentGlobalAbstract;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class NotesFragment extends FragmentGlobalAbstract {
    static NotesFragment instance;

    static public NotesFragment getInstance() {
        if (instance == null)
            instance = new NotesFragment();

        return instance;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
