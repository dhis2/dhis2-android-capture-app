package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.dhis2.data.forms.FormSectionViewModel;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import java.util.Arrays;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 *
 */

public class EventInfoSectionsRepositoryImpl implements EventInfoSectionsRepository {

    private final BriteDatabase briteDatabase;

    private static final List<String> SECTION_TABLES = Arrays.asList(
            EventModel.TABLE, ProgramModel.TABLE, ProgramStageModel.TABLE, ProgramStageSectionModel.TABLE);

    private static final String SELECT_SECTIONS = "SELECT\n" +
            "  Program.uid AS programUid,\n" +
            "  ProgramStage.uid AS programStageUid,\n" +
            "  ProgramStageSection.uid AS programStageSectionUid,\n" +
            "  ProgramStageSection.displayName AS programStageDisplayName\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "  LEFT OUTER JOIN ProgramStageSection ON ProgramStageSection.programStage = Event.programStage\n" +
            "WHERE Event.uid = ?";

    EventInfoSectionsRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> sections(@NonNull String eventUid) {
//        String SELECT_PROGRAM_STAGE_SECTIONS = "SELECT * FROM " + ProgramStageSectionModel.TABLE + " WHERE " + ProgramStageSectionModel.Columns.PROGRAM_STAGE + " = '" + programStageUid + "'";
//        return briteDatabase.createQuery(ProgramStageSectionModel.TABLE, SELECT_PROGRAM_STAGE_SECTIONS).mapToList(ProgramStageSectionModel::create);

        return briteDatabase
                .createQuery(SECTION_TABLES, SELECT_SECTIONS, eventUid)
                .mapToList(cursor -> mapToFormSectionViewModels(eventUid, cursor))
                .distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private FormSectionViewModel mapToFormSectionViewModels(
            @NonNull String eventUid, @NonNull Cursor cursor) {
        if (cursor.getString(2) == null) {
            // This programstage has no sections
            return FormSectionViewModel.createForProgramStage(
                    eventUid, cursor.getString(1));
        } else {
            // This programstage has sections
            return FormSectionViewModel.createForSection(
                    eventUid, cursor.getString(2), cursor.getString(3));
        }
    }
}