package org.dhis2.usescases.programDetail;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;

import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_TABLE;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramRepositoryImpl implements ProgramRepository {

    private final BriteDatabase briteDatabase;
    private static final String SELECT_TEI = "SELECT TrackedEntityInstance.* FROM TrackedEntityInstance " +
            "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityInstance.uid \n" +
            "JOIN Event ON Event.enrollment = Enrollment.uid \n" +
            "WHERE Enrollment.program = ? GROUP BY TrackedEntityInstance.uid ORDER BY Event.eventDate";

    private static final String SELECT_PROGRAM_ATTRIBUTES_FOR_TEI = "SELECT * FROM TrackedEntityAttributeValue " +
            "WHERE trackedEntityAttribute IN " +
            "(SELECT ProgramTrackedEntityAttribute.trackedEntityAttribute FROM ProgramTrackedEntityAttribute " +
            "WHERE ProgramTrackedEntityAttribute.program = ? AND displayInList = 1) " +
            "AND trackedEntityInstance = ?";

    private static final String SELECT_PROGRAM_STAGE_FOR_TEI = "SELECT ProgramStage.* FROM ProgramStage " +
            "JOIN Event ON Event.programStage = ProgramStage.uid " +
            "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
            "WHERE ProgramStage.program = ? AND Enrollment.trackedEntityInstance = ? GROUP BY ProgramStage.uid ORDER BY Event.eventDate DESC";

    private static final String SELECT_ENROLLMENT_FOR_TEI = "SELECT * FROM Enrollment " +
            "WHERE Enrollment.program = ? AND Enrollment.trackedEntityInstance = ? ORDER BY Enrollment.enrollmentDate DESC";

    public ProgramRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances(String programUid) {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_TEI, programUid == null ? "" : programUid)
                .mapToList(TrackedEntityInstanceModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> programAttributesValues(String programId, String teiUid) {
        return briteDatabase.createQuery(TrackedEntityAttributeValueModel.TABLE, SELECT_PROGRAM_ATTRIBUTES_FOR_TEI,
                programId == null ? "" : programId,
                teiUid == null ? "" : teiUid)
                .mapToList(TrackedEntityAttributeValueModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStage>> programStage(String programStageId, String teiUid) {
        return briteDatabase.createQuery(PROGRAM_STAGE_TABLE, SELECT_PROGRAM_STAGE_FOR_TEI,
                programStageId == null ? "" : programStageId,
                teiUid == null ? "" : teiUid)
                .mapToList(ProgramStage::create);
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentModel>> enrollments(String programId, String teiUid) {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_ENROLLMENT_FOR_TEI,
                programId == null ? "" : programId,
                teiUid == null ? "" : teiUid)
                .mapToList(EnrollmentModel::create);
    }
}