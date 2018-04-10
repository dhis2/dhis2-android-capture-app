package com.dhis2.usescases.programDetail;

import android.databinding.BaseObservable;

import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.List;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class TrackedEntityObject extends BaseObservable {

    private List<MyTrackedEntityInstance> myTrackedEntityInstances;
    private List<ProgramTrackedEntityAttributeModel> programTrackedEntityAttributes;

    TrackedEntityObject(List<MyTrackedEntityInstance> trackedEntityInstances, List<ProgramTrackedEntityAttributeModel> programTrackedEntityAttributes) {
        this.myTrackedEntityInstances = trackedEntityInstances;
        this.programTrackedEntityAttributes = programTrackedEntityAttributes;
    }

    public List<MyTrackedEntityInstance> getMyTrackedEntityInstances() {
        return myTrackedEntityInstances;
    }

    public List<ProgramTrackedEntityAttributeModel> getProgramTrackedEntityAttributes() {
        return programTrackedEntityAttributes;
    }
}
