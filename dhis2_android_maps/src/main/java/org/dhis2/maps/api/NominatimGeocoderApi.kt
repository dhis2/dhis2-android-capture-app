package org.dhis2.maps.api

import io.ktor.http.headers
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.maps.model.NominatimLocation
import org.hisp.dhis.android.BuildConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import timber.log.Timber
import java.util.Locale

private const val SEARCH_API = "https://nominatim.openstreetmap.org/reverse?"
private const val REVERSE_API = "https://nominatim.openstreetmap.org/search?"
private const val HEADER_REFERER = "Referer"
private const val HEADER_LANGUAGE = "Accept-Language"
private const val PARAM_FORMAT = "format"
private const val PARAM_FORMAT_JSON = "json"
private const val PARAM_LATITUDE = "lat"
private const val PARAM_LONGITUDE = "lon"
private const val PARAM_QUERY = "q"
private const val PARAM_LIMIT = "limit"

class NominatimGeocoderApi(
    private val d2: D2,
    private val localeSelector: LocaleSelector,
) : GeocoderApi {
    override suspend fun searchFor(query: String, maxResults: Int): List<LocationItemModel> {
        return d2.httpServiceClient()
            .get<List<NominatimLocation>> {
                url(REVERSE_API)
                customHeaders()
                parameters {
                    attribute(Pair(PARAM_FORMAT, PARAM_FORMAT_JSON))
                    attribute(Pair(PARAM_QUERY, query))
                    attribute(Pair(PARAM_LIMIT, maxResults))
                }
            }.mapNominatimLocationsToLocationItems()
    }

    override suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel.SearchResult {
        return try {
            d2.httpServiceClient()
                .get<NominatimLocation> {
                    url(SEARCH_API)
                    customHeaders()
                    parameters {
                        attribute(
                            Pair(
                                PARAM_FORMAT,
                                PARAM_FORMAT_JSON,
                            ),
                        )
                        attribute(Pair(PARAM_LATITUDE, latitude.toString()))
                        attribute(Pair(PARAM_LONGITUDE, longitude.toString()))
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

    private fun customHeaders() {
        headers {
            append(
                HEADER_REFERER,
                "${BuildConfig.LIBRARY_PACKAGE_NAME}_${BuildConfig.VERSION_NAME}",
            )
            append(
                HEADER_LANGUAGE,
                localeSelector.getUserLanguage() ?: Locale.getDefault().language,
            )
        }
    }

    private fun List<NominatimLocation>.mapNominatimLocationsToLocationItems() =
        map { nominatimLocation ->
            nominatimLocation.toLocationItem()
        }

    private fun NominatimLocation.toLocationItem() = LocationItemModel.SearchResult(
        searchedTitle = name,
        searchedSubtitle = displayName.removePrefix(name),
        searchedLatitude = lat.toDouble(),
        searchedLongitude = lon.toDouble(),
    )
}
