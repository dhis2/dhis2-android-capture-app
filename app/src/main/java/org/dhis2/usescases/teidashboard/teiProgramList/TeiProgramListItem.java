package org.dhis2.usescases.teidashboard.teiProgramList;

import androidx.annotation.IntDef;

import org.dhis2.usescases.main.program.ProgramViewModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * QUADRAM. Created by Cristian on 08/03/2018.
 */

public class TeiProgramListItem {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TeiProgramListItemViewType.ALL_PROGRAMS_DASHBOARD,TeiProgramListItemViewType.FIRST_TITLE, TeiProgramListItemViewType.ACTIVE_ENROLLMENT,
            TeiProgramListItemViewType.PROGRAM, TeiProgramListItemViewType.SECOND_TITLE,
            TeiProgramListItemViewType.INACTIVE_ENROLLMENT, TeiProgramListItemViewType.THIRD_TITLE,
            TeiProgramListItemViewType.PROGRAMS_TO_ENROLL})
    public @interface TeiProgramListItemViewType {
        int FIRST_TITLE = 0;
        int ACTIVE_ENROLLMENT = 1;
        int PROGRAM = 2;
        int SECOND_TITLE = 3;
        int INACTIVE_ENROLLMENT = 4;
        int THIRD_TITLE = 5;
        int PROGRAMS_TO_ENROLL = 6;
        int ALL_PROGRAMS_DASHBOARD = 7;
    }

    private EnrollmentViewModel enrollmentModel;
    private ProgramViewModel programModel;
    private @TeiProgramListItemViewType
    int viewType;

    public TeiProgramListItem(EnrollmentViewModel enrollmentModel, ProgramViewModel programModel, int viewType) {
        this.enrollmentModel = enrollmentModel;
        this.programModel = programModel;
        this.viewType = viewType;
    }

    public EnrollmentViewModel getEnrollmentModel() {
        return enrollmentModel;
    }

    public ProgramViewModel getProgramModel() {
        return programModel;
    }

    public int getViewType() {
        return viewType;
    }
}
