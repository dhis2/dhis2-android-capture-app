package org.dhis2.maps.api

import kotlinx.coroutines.delay
import org.dhis2.commons.extensions.truncate
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.maps.model.NominatimLocation
import org.dhis2.maps.utils.AvailableLatLngBounds
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
        visibleRegion: AvailableLatLngBounds?,
        maxResults: Int,
    ): List<LocationItemModel> {
        if (query.isEmpty()) return emptyList()

        val startTime = System.currentTimeMillis()

        val searchResult: MutableList<NominatimLocation> = mutableListOf()
        run loop@{
            visibleRegion?.list?.forEach { region ->
                var southEastLatitude = region.southEast.latitude
                var southEastLongitude = region.southEast.longitude
                if (region.northWest.latitude == region.southEast.latitude) {
                    southEastLatitude = -southEastLatitude
                }
                if (region.northWest.longitude == region.southEast.longitude) {
                    southEastLongitude = -southEastLongitude
                }

                searchResult.addAll(
                    search(
                        query = query,
                        topCornerLongitude = region.northWest.longitude,
                        bottomCornerLongitude = southEastLongitude,
                        topCornerLatitude = region.northWest.latitude,
                        bottomCornerLatitude = southEastLatitude,
                        maxResults = maxResults - searchResult.size,
                    ),
                )

                if (searchResult.size == maxResults) {
                    return@loop
                }
                while (System.currentTimeMillis() - startTime < 1000) {
                    delay(100)
                }
            }
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
    ): LocationItemModel.SearchResult =
        try {
            d2
                .httpServiceClient()
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

    private fun NominatimLocation.toLocationItem() =
        LocationItemModel.SearchResult(
            searchedTitle = name,
            searchedSubtitle =
                displayName?.removePrefix("$name, ") ?: "Lat:${
                    lat.toDouble().truncate()
                } Lon:${lon.toDouble().truncate()}",
            searchedLatitude = lat.toDouble(),
            searchedLongitude = lon.toDouble(),
        )
}
