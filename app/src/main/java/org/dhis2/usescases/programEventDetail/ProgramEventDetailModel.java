package org.dhis2.usescases.programEventDetail;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.List;

/**
 * Created by Cristian on 22/02/2018.
 *
 */

public class ProgramEventDetailModel {
    private List<EventModel> events;
    private List<ProgramTrackedEntityAttributeModel> attributesToShow;
}
