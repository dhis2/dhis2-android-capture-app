package com.dhis2.usescases.programDetailTablet;

import org.hisp.dhis.android.core.common.Pager;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class TrackedEntityObject {

    private Pager pager;
    private ArrayList<TrackedEntityInstance> trackedEntityInstances;
    private List<ProgramTrackedEntityAttributeModel> programTrackedEntityAttributes;

    public ArrayList<TrackedEntityInstance> getTrackedEntityInstances() {
        return trackedEntityInstances;
    }


    public Pager getPager() {
        return pager;
    }

    public List<ProgramTrackedEntityAttributeModel> getProgramTrackedEntityAttributes() {
        return programTrackedEntityAttributes;
    }

    public void setProgramTrackedEntityAttributes(List<ProgramTrackedEntityAttributeModel> programTrackedEntityAttributes) {
        this.programTrackedEntityAttributes = programTrackedEntityAttributes;
    }
}
