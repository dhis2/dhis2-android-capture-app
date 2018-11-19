package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureRepositoryImpl implements EventCaptureContract.EventCaptureRepository {

    private static final List<String> SECTION_TABLES = Arrays.asList(
            EventModel.TABLE, ProgramModel.TABLE, ProgramStageModel.TABLE, ProgramStageSectionModel.TABLE);
    private static final String SELECT_SECTIONS = "SELECT\n" +
            "  Program.uid AS programUid,\n" +
            "  ProgramStage.uid AS programStageUid,\n" +
            "  ProgramStageSection.uid AS programStageSectionUid,\n" +
            "  ProgramStageSection.displayName AS programStageSectionDisplayName,\n" +
            "  ProgramStage.displayName AS programStageDisplayName,\n" +
            "  ProgramStageSection.mobileRenderType AS renderType\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "  LEFT OUTER JOIN ProgramStageSection ON ProgramStageSection.programStage = Event.programStage\n" +
            "WHERE Event.uid = ?\n" +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' ORDER BY ProgramStageSection.sortOrder";

    private final BriteDatabase briteDatabase;
    private final String eventUid;

    public EventCaptureRepositoryImpl(BriteDatabase briteDatabase, String eventUid) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
    }

    @Override
    public Flowable<String> programStageName() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                "SELECT ProgramStage.* FROM ProgramStage " +
                        "JOIN Event ON Event.programStage = ProgramStage.uid " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOne(cursor -> ProgramStageModel.create(cursor).displayName())
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<String> eventDate() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                "SELECT Event.* FROM Event " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOne(cursor -> EventModel.create(cursor).eventDate())
                .map(eventDate -> DateUtils.uiDateFormat().format(eventDate))
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<String> orgUnit() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                        "JOIN Event ON Event.organisationUnit = OrganisationUnit.uid " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOne(cursor -> OrganisationUnitModel.create(cursor).displayName())
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<String> catOption() {
        return briteDatabase.createQuery(CategoryOptionModel.TABLE,
                "SELECT CategoryOption.* FROM CategoryOption " +
                        "JOIN Event ON Event.attributeCategoryOptions = CategoryOption.uid " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOne(cursor -> CategoryOptionModel.create(cursor).displayName())
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<List<FormSectionViewModel>> eventSections() {
        return briteDatabase
                .createQuery(SECTION_TABLES, SELECT_SECTIONS, eventUid)
                .mapToList(this::mapToFormSectionViewModels)
                .distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST);
    }

    private HashMap<String, Pair<FormSectionViewModel, Boolean>> switchToMap(List<FormSectionViewModel> list) {
        HashMap<String, Pair<FormSectionViewModel, Boolean>> sectionsMap= new HashMap<>();
        for(FormSectionViewModel formSection : list){
            sectionsMap.put(formSection.sectionUid(),Pair.create(formSection,true));
        }
        return sectionsMap;
    }

    @NonNull
    private FormSectionViewModel mapToFormSectionViewModels(@NonNull Cursor cursor) {
        // GET PROGRAMSTAGE DISPLAYNAME IN CASE THERE ARE NO SECTIONS
        if (cursor.getString(2) == null) {
            // This programstage has no sections
            return FormSectionViewModel.createForProgramStageWithLabel(eventUid, cursor.getString(4), cursor.getString(1));
        } else {
            // This programstage has sections
            return FormSectionViewModel.createForSection(eventUid, cursor.getString(2), cursor.getString(3), cursor.getString(5));
        }
    }
}
