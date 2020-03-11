package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface ProgramEventDetailRepository {

    @NonNull
    LiveData<PagedList<ProgramEventViewModel>> filteredProgramEvents(List<DatePeriod> dateFilter, List<String> orgUnitFilter, List<CategoryOptionCombo> catOptionComboUid, List<EventStatus> eventStatus, List<State> states, boolean assignedToUser);

    @NonNull
    Flowable<kotlin.Pair<FeatureCollection, BoundingBox>> filteredEventsForMap(List<DatePeriod> dateFilter, List<String> orgUnitFilter, List<CategoryOptionCombo> catOptionComboUid, List<EventStatus> eventStatus, List<State> states, boolean assignedToUser);

    @NonNull
    Observable<Program> program();

    boolean getAccessDataWrite();

    Single<Pair<CategoryCombo, List<CategoryOptionCombo>>> catOptionCombos();

    Single<Boolean> hasAccessToAllCatOptions();

    Flowable<ProgramEventViewModel> getInfoForEvent(String eventUid);

    Single<FeatureType> featureType();
}
