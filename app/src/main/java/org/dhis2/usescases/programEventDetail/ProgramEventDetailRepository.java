package org.dhis2.usescases.programEventDetail;

import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 */

public interface ProgramEventDetailRepository {

    @NonNull
    Flowable<List<ProgramEventViewModel>> filteredProgramEvents(String programUid, List<Date> dates, Period period, CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery, int page);

    @NonNull
    Flowable<List<ProgramEventViewModel>> filteredProgramEvents(List<DatePeriod> dateFilter, List<String> orgUnitFilter, int page);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits(String parentUid);

    @NonNull
    Observable<List<CategoryOptionComboModel>> catCombo(String programUid);

    Observable<List<String>> eventDataValuesNew(EventModel eventModel);

    Observable<Boolean> writePermission(String programId);
}
