package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.maps.layer.MapLayer
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.event.EventFilter
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage

interface ProgramEventDetailRepository {
    fun filteredProgramEvents(): LiveData<PagedList<EventViewModel>>
    fun filteredEventsForMap(layersVisibility: Map<String, MapLayer>): Flowable<ProgramEventMapData>
    fun program(): Single<Program?>
    fun getAccessDataWrite(): Boolean
    fun getInfoForEvent(eventUid: String): Flowable<ProgramEventViewModel>
    fun featureType(): Single<FeatureType>
    fun getCatOptCombo(selectedCatOptionCombo: String): CategoryOptionCombo?
    fun workingLists(): Single<List<EventFilter>>
    fun programStage(): Single<ProgramStage?>
    fun programHasCoordinates(): Boolean
    fun programHasAnalytics(): Boolean
    fun isEventEditable(eventUid: String): Boolean
    fun displayOrganisationUnit(programUid: String): Boolean
}
