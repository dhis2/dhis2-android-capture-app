package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.search.model.SearchTrackedEntityAttribute
import org.dhis2.tracker.search.model.SearchTrackerParameterResult
import org.dhis2.tracker.search.model.SearchTrackerParametersModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository

class LoadSearchResultsRepositoryImpl(
    private val d2: D2,
    private val filterPresenter: FilterPresenter,
    private val customIntentRepository: CustomIntentRepository,
): LoadSearchResultsRepository {

    var trackedEntityInstanceQuery: TrackedEntitySearchCollectionRepository? = null

    override suspend fun isTETypeAttribute(
        teType: String,
        dataId: String
    ): Boolean {
       return d2.trackedEntityModule().trackedEntityTypeAttributes()
           .byTrackedEntityTypeUid().eq(teType)
           .byTrackedEntityAttributeUid().eq(dataId).one().blockingExists()
    }

    override suspend fun getTEAttribute(dataId: String): SearchTrackedEntityAttribute {
        val attribute =
            d2.trackedEntityModule().trackedEntityAttributes().uid(dataId).blockingGet()
        return SearchTrackedEntityAttribute(
            isUnique = attribute?.unique() == true,
            isOptionSet = (attribute?.optionSet() != null)
        )
    }

    override suspend fun addToQuery(
        dataId: String,
        dataValues: List<String>,
        isUnique: Boolean,
        isOptionSet: Boolean
    ) {

        if (dataValues.size > 1) {
            // return any tracked entities with attributes that match the the values in the list
             trackedEntityInstanceQuery?.byFilter(dataId)?.`in`(dataValues)
        } else {
            if (dataValues.size == 1) {
                var dataValue = dataValues.get(0)
                if (isUnique || isOptionSet) {
                    // If the attribute is unique or an option set, we want an exact match
                     trackedEntityInstanceQuery?.byFilter(dataId)?.eq(dataValue)
                } else if (dataValue.contains(OPTION_SET_REGEX)) {
                    //legacy code could no longer be needed
                    dataValue = dataValue.split(OPTION_SET_REGEX.toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                     trackedEntityInstanceQuery?.byFilter(dataId)?.eq(dataValue)
                } else  // return tracked entities that contain the data value
                     trackedEntityInstanceQuery?.byFilter(dataId)?.like(dataValue)
            } else {
                 trackedEntityInstanceQuery
            }
        }
        TODO("Not yet implemented")
    }

    override suspend fun addFiltersToQuery(program: String?, teType: String) {
        trackedEntityInstanceQuery = filterPresenter.filteredTrackedEntityInstances(
            program, teType
        )
    }

    override suspend fun getResults(
    ): Flow<PagingData<SearchTrackerParameterResult>> {

    }


    fun getFilteredRepository(searchTrackerParametersModel: SearchTrackerParametersModel): TrackedEntitySearchCollectionRepository? {

     trackedEntityInstanceQuery = filterPresenter.filteredTrackedEntityInstances(
            searchTrackerParametersModel.selectedProgram, teiType
        )
     searchTrackerParametersModel.queryData?.let {
         for (i in it.keys.indices) {
             val dataId = searchTrackerParametersModel.queryData.keys.toTypedArray()[i]
             var dataValues: MutableList<String>? = searchTrackerParametersModel.queryData[dataId]?.toMutableList()

             val isTETypeAttribute = d2.trackedEntityModule().trackedEntityTypeAttributes()
                 .byTrackedEntityTypeUid().eq(teiType)
                 .byTrackedEntityAttributeUid().eq(dataId).one().blockingExists()

             if (searchTrackerParametersModel.selectedProgram != null || isTETypeAttribute) {
                 val attribute =
                     d2.trackedEntityModule().trackedEntityAttributes().uid(dataId).blockingGet()
                 val isUnique: Boolean = attribute!!.unique()!!
                 val isOptionSet = (attribute.optionSet() != null)
                 checkNotNull(dataValues)
                 if (!customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                         dataId,
                         CustomIntentActionTypeModel.SEARCH
                     ) && dataValues.size > 1
                 ) {
                     //Only search with a list of values when the attribute is linked to a custom intent
                     //that returns a list of values, otherwise the comma was one of the search characters
                     dataValues = mutableListOf<String>(dataValues.joinToString(","))
                 }
                 trackedEntityInstanceQuery =
                     getTrackedEntityQuery(dataId, dataValues, isUnique, isOptionSet)
             }
         }

     }
        return trackedEntityInstanceQuery

    }

    private fun getTrackedEntityQuery(
        dataId: String,
        dataValues: MutableList<String>,
        isUnique: Boolean,
        isOptionSet: Boolean
    ): TrackedEntitySearchCollectionRepository? {
        if (dataValues.size > 1) {
            // return any tracked entities with attributes that match the the values in the list
            return trackedEntityInstanceQuery?.byFilter(dataId)?.`in`(dataValues)
        } else {
            if (dataValues.size == 1) {
                var dataValue = dataValues.get(0)
                if (isUnique || isOptionSet) {
                    // If the attribute is unique or an option set, we want an exact match
                    return trackedEntityInstanceQuery?.byFilter(dataId)?.eq(dataValue)
                } else if (dataValue.contains(OPTION_SET_REGEX)) {
                    //legacy code could no longer be needed
                    dataValue = dataValue.split(OPTION_SET_REGEX.toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    return trackedEntityInstanceQuery?.byFilter(dataId)?.eq(dataValue)
                } else  // return tracked entities that contain the data value
                    return trackedEntityInstanceQuery?.byFilter(dataId)?.like(dataValue)
            } else {
                return trackedEntityInstanceQuery
            }
        }
    }

    companion object {
        const val  OPTION_SET_REGEX = "_os_"
    }

}