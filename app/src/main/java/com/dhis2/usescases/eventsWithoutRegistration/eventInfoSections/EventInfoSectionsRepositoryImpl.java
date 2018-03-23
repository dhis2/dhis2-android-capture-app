package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 *
 */

public class EventInfoSectionsRepositoryImpl implements EventInfoSectionsRepository {

    private final BriteDatabase briteDatabase;

    EventInfoSectionsRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageSectionModel>> programStageSections(@NonNull String programStageUid) {
        String SELECT_PROGRAM_STAGE_SECTIONS = "SELECT * FROM " + ProgramStageSectionModel.TABLE + " WHERE " + ProgramStageSectionModel.Columns.PROGRAM_STAGE + " = '" + programStageUid + "'";
        return briteDatabase.createQuery(ProgramStageSectionModel.TABLE, SELECT_PROGRAM_STAGE_SECTIONS).mapToList(ProgramStageSectionModel::create);
    }
}