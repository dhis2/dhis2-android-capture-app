package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.dhis2.data.filter.TextFilter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.event.EventFilter;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface ProgramEventDetailRepository {

    LiveData<PagedList<EventViewModel>> filteredProgramEvents(TextFilter textFilter);

    @NonNull
    Flowable<ProgramEventMapData> filteredEventsForMap();

    @NonNull
    Observable<Program> program();

    boolean getAccessDataWrite();

    Single<Boolean> hasAccessToAllCatOptions();

    Flowable<ProgramEventViewModel> getInfoForEvent(String eventUid);

    Single<FeatureType> featureType();

    CategoryOptionCombo getCatOptCombo(String selectedCatOptionCombo);

    Single<List<EventFilter>> workingLists();

    Single<ProgramStage> programStage();

    boolean programHasCoordinates();

    boolean programHasAnalytics();

    Observable<List<DataElement>> textTypeDataElements();
}
