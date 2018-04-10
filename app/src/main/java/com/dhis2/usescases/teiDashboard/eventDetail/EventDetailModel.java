package com.dhis2.usescases.teiDashboard.eventDetail;

import android.databinding.BaseObservable;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Cristian on 08/02/2018.
 *
 */

public class EventDetailModel extends BaseObservable {

    private final List<ProgramStageDataElementModel> dataElemets;
    private final List<ProgramStageSectionModel> stageSections;
    private final HashMap<String, List<ProgramStageDataElementModel>> fieldsElements;
    private EventModel eventModel;
    private List<TrackedEntityDataValueModel> dataValueModelList;

    EventDetailModel(EventModel eventModel, List<TrackedEntityDataValueModel> dataValueModelList,
                            List<ProgramStageSectionModel> programStageSectionModelList, List<ProgramStageDataElementModel> programStageDataElementModelList) {
        this.eventModel = eventModel;
        this.dataValueModelList = dataValueModelList;
        this.dataElemets = programStageDataElementModelList;
        this.stageSections = programStageSectionModelList;
        fieldsElements = new HashMap<>();

        setUpFields();

    }

    private void setUpFields() {

        ArrayList<ProgramStageDataElementModel> sectionDataElements = new ArrayList<>();
        for (ProgramStageDataElementModel de : dataElemets) {
            if (de.programStageSection() == null)
                sectionDataElements.add(de);
        }
        fieldsElements.put("null", sectionDataElements);

        for (ProgramStageSectionModel section : stageSections) {
            sectionDataElements = new ArrayList<>();
            for (ProgramStageDataElementModel de : dataElemets)
                if (de.programStageSection() != null && de.programStageSection().equals(section.uid()))
                    sectionDataElements.add(de);
            fieldsElements.put(section.uid(), sectionDataElements);
        }
    }

    EventModel getEventModel() {
        return eventModel;
    }

    List<ProgramStageSectionModel> getStageSections() {
        return stageSections;
    }

    List<ProgramStageDataElementModel> getDataElementsForSection(String sectionUid) {
        if (sectionUid != null)
            return fieldsElements.get(sectionUid);
        else
            return dataElemets;
    }

    String getValueForDE(String dataelementUid) {
        for (TrackedEntityDataValueModel trackedEntityDataValueModel : dataValueModelList) {
            if (trackedEntityDataValueModel.dataElement().equals(dataelementUid))
                return trackedEntityDataValueModel.value();
        }
        return null;
    }
}
