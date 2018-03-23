package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface EventInfoSectionsRepository {


    @NonNull
    Observable<List<ProgramStageSectionModel>> programStageSections(@NonNull String programStageUid);

}
