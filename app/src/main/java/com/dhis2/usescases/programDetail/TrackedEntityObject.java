package com.dhis2.usescases.programDetail;

import org.hisp.dhis.android.core.common.Pager;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class TrackedEntityObject {

    private Pager pager;
    private ArrayList<TrackedEntityInstance> trackedEntityInstances;

    public ArrayList<TrackedEntityInstance> getTrackedEntityInstances() {
        return trackedEntityInstances;
    }

    public Pager getPager() {
        return pager;
    }
}
