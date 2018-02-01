package com.dhis2.usescases.enrollment;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.List;

/**
 * Created by ppajuelo on 31/01/2018.
 */
@AutoValue
public abstract class EnrollmentZipData {

    @NonNull
    abstract List<ProgramTrackedEntityAttributeModel> programTEAttributes();

    @NonNull
    abstract ProgramModel program();

    @NonNull
    public static EnrollmentZipData createZipData(@NonNull List<ProgramTrackedEntityAttributeModel> programTEAttributes, @NonNull ProgramModel program) {
        return new AutoValue_EnrollmentZipData(programTEAttributes, program);
    }

}
