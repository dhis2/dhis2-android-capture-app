package org.dhis2.usescases.programStageSelection;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.List;

import androidx.annotation.NonNull;

public class ProgramStageSelectionContract {

    public interface View extends AbstractActivityContracts.View {

        void setData(List<ProgramStage> programStages);

        void setResult(String programStageUid, boolean repeatable, PeriodType periodType);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void onBackClick();

        void getProgramStages(String programId, @NonNull String programUid);

        void onProgramStageClick(ProgramStage programStage);

        int getStandardInterval(String programStageUid);
    }
}
