package org.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public interface ProgramRepository {

    @NonNull
    Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances(String programUid);

    @NonNull
    Observable<List<TrackedEntityAttributeValueModel>> programAttributesValues(String programId, String teiUid);

    @NonNull
    Observable<List<ProgramStageModel>> programStage(String programStageId, String teiUid);

    @NonNull
    Observable<List<EnrollmentModel>> enrollments(String programId, String teiUid);

}
