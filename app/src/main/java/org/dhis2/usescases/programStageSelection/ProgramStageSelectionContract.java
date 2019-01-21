package org.dhis2.usescases.programStageSelection;

import androidx.annotation.NonNull;

import org.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

public class ProgramStageSelectionContract {

    public interface View extends AbstractActivityContracts.View {

        void setData(List<ProgramStageModel> programStageModels);

        void setResult(String programStageUid, boolean repeatable, PeriodType periodType);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void onBackClick();

        void getProgramStages(String programId, @NonNull String programUid, @NonNull View view);

        void onProgramStageClick(ProgramStageModel programStage);
    }
}
