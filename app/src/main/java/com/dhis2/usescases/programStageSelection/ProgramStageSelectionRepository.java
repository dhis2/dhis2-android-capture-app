package com.dhis2.usescases.programStageSelection;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public interface ProgramStageSelectionRepository {

    @NonNull
    Observable<List<ProgramStageModel>> getProgramStages(String programStages);
}
