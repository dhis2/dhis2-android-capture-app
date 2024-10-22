package org.dhis2.maps.api

import kotlinx.coroutines.delay
import org.dhis2.commons.extensions.truncate
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.maps.model.NominatimLocation
import org.hisp.dhis.android.BuildConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.api.RequestBuilder
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import timber.log.Timber
import java.util.Locale

private const val REVERSE_API = "https://nominatim.openstreetmap.org/reverse?"
private const val SEARCH_API = "https://nominatim.openstreetmap.org/search?"
private const val HEADER_REFERER = "Referer"
private const val HEADER_LANGUAGE = "Accept-Language"
private const val PARAM_FORMAT = "format"
private const val PARAM_FORMAT_JSON = "json"
private const val PARAM_LATITUDE = "lat"
private const val PARAM_LONGITUDE = "lon"
private const val PARAM_QUERY = "q"
private const val PARAM_LIMIT = "limit"
private const val PARAM_VIEWBOX = "viewbox"
private const val PARAM_BOUNDED = "bounded"

class NominatimGeocoderApi(
    private val d2: D2,
    private val localeSelector: LocaleSelector,
) : GeocoderApi {
    override suspend fun searchFor(
        query: String,
        topCornerLatitude: Double?,
        topCornerLongitude: Double?,
        bottomCornerLatitude: Double?,
        bottomCornerLongitude: Double?,
        maxResults: Int,
    ): List<LocationItemModel> {
        if (query.isEmpty()) return emptyList()

        val startTime = System.currentTimeMillis()
        var searchResult = search(
            query = query,
            topCornerLongitude = topCornerLongitude,
            bottomCornerLongitude = bottomCornerLongitude,
            topCornerLatitude = topCornerLatitude,
            bottomCornerLatitude = bottomCornerLatitude,
            maxResults = maxResults,
        )
        if (searchResult.isEmpty()) {
            while (System.currentTimeMillis() - startTime < 1000) {
                delay(100)
            }
            searchResult = search(
                query = query,
                topCornerLongitude = topCornerLongitude,
                bottomCornerLongitude = bottomCornerLongitude,
                topCornerLatitude = topCornerLatitude,
                bottomCornerLatitude = bottomCornerLatitude,
                maxResults = maxResults,
                bounded = 0,
            )
        }
        return searchResult.mapNominatimLocationsToLocationItems()
    }

    private suspend fun search(
        query: String,
        topCornerLongitude: Double?,
        bottomCornerLongitude: Double?,
        topCornerLatitude: Double?,
        bottomCornerLatitude: Double?,
        maxResults: Int,
        bounded: Int = 1,
    ) = d2.httpServiceClient().get<List<NominatimLocation>> {
        absoluteUrl(SEARCH_API)
        customHeaders()
        parameters {
            attribute(PARAM_FORMAT, PARAM_FORMAT_JSON)
            attribute(PARAM_QUERY, query)
            attribute(PARAM_LIMIT, maxResults)
            topCornerLatitude?.let {
                attribute(
                    PARAM_VIEWBOX,
                    "$topCornerLongitude,$topCornerLatitude,$bottomCornerLongitude,$bottomCornerLatitude",
                )
                attribute(PARAM_BOUNDED, bounded)
            }
        }
    }

    override suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel.SearchResult {
        return try {
            d2.httpServiceClient()
                .get<NominatimLocation> {
                    url(REVERSE_API)
                    customHeaders()
                    parameters {
                        attribute(PARAM_FORMAT, PARAM_FORMAT_JSON)
                        attribute(PARAM_LATITUDE, latitude.toString())
                        attribute(PARAM_LONGITUDE, longitude.toString())
                    }
                }.toLocationItem()
        } catch (e: Exception) {
            Timber.e(e)
            LocationItemModel.SearchResult(
                searchedTitle = "",
                searchedSubtitle = "",
                searchedLatitude = latitude,
                searchedLongitude = longitude,
            )
        }
    }

    private fun RequestBuilder.customHeaders() {
        header(
            HEADER_REFERER,
            "${BuildConfig.LIBRARY_PACKAGE_NAME}_${BuildConfig.VERSION_NAME}",
        )
        header(
            HEADER_LANGUAGE,
            localeSelector.getUserLanguage() ?: Locale.getDefault().language,
        )
    }

    private fun List<NominatimLocation>.mapNominatimLocationsToLocationItems() =
        map { nominatimLocation ->
            nominatimLocation.toLocationItem()
        }

    private fun NominatimLocation.toLocationItem() = LocationItemModel.SearchResult(
        searchedTitle = name,
        searchedSubtitle = display_name?.removePrefix("$name, ") ?: "Lat:${
            lat.toDouble().truncate()
        } Lon:${lon.toDouble().truncate()}",
        searchedLatitude = lat.toDouble(),
        searchedLongitude = lon.toDouble(),
    )
}
