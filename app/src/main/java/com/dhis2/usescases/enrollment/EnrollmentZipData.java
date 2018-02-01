package com.dhis2.usescases.enrollment;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.List;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public class EnrollmentZipData {

    private final List<ProgramTrackedEntityAttributeModel> programTEAttributes;
    private final ProgramModel program;

    public EnrollmentZipData(List<ProgramTrackedEntityAttributeModel> programTEAttributes, ProgramModel program) {
        this.programTEAttributes = programTEAttributes;
        this.program = program;
    }

}
