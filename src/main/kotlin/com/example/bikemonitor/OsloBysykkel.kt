package com.example.bikemonitor

import com.google.gson.Gson
import khttp.get
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * An example implementation of GeneralBikeShareFeedSpecification service using Oslo Bysykkel open
 * API.
 */
@Component
class OsloBysykkel : GeneralBikeShareFeedSpecification {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val gson = Gson()
    // TODO Move this out into some sort of configuration
    override val gbfsBaseUrl = "https://gbfs.urbansharing.com/oslobysykkel.no/"

    /**
     * Main logic behind the /stations/availability endpoint. Combines data from two Oslo Bysykkel
     * APIs, info about stations themselves and their status details, including how many bikes are
     * available, their max capacity (number of working docking stations) and number of empty docking stations to which one can return a bike.
     *
     * @return JSON server reponse with last_updated Unix timestamp and list of stations with their
     * names, bike availability and number of empty docking stations to which one can return a bike..
     */
    override fun getBikeAvailability(request: ServerRequest): Mono<ServerResponse> {
        val StationsInfoResponse = getStationsInfoData(getGBFSUrl("station_information.json"))
        val StationsStatusResponse = getStationsStatusData(getGBFSUrl("station_status.json"))

        // Grouping stations details and their statuses by station_id to combine them easily
        var stationsByStationId = StationsInfoResponse.data.stations.map { it.station_id to it }.toMap()
        var statusByStationId = StationsStatusResponse.data.stations.map { it.station_id to it }.toMap()
        // Do we have info on all stations in the status response?
        // We may have details for more stations than present in the status response which is fine.
        // If vice versa is true, it means our list of stations details is outdated or incomplete.
        val allStationsKnown =
            (stationsByStationId.keys.toSet().containsAll(statusByStationId.keys.toSet()))
        if (allStationsKnown != true) {
            // TODO Should we download the station information again?
            // Throwing an error might be an overkill if it's only some stations that are missing info,
            // not all. Depends what the client needs and how strict we should be.
            TODO("Not implemented yet")
        }

        // Combine the stations info and statuses
        val stationsWithAvailability =
            getStationsWithAvaliability(stationsByStationId, statusByStationId)

        // In the response JSON mirror the GBFS structure
        val responseBody =
            StationsWithAvailabilityJSON(
                last_updated = Instant.now().epochSecond,
                StationsWithAvailability(stations = stationsWithAvailability)
            )
        return ServerResponse.ok().body(Mono.just(responseBody))
    }

    /**
     * Combine stations names and info about their capacity and bike availability.file
     *
     * @return list of objects with the data combined
     */
    fun getStationsWithAvaliability(
        stationsByStationId: Map<String, StationInfo>,
        availabilityByStationId: Map<String, StationStatus>
    ): MutableList<BikeStationWithAvailability> {
        val stationsWithAvailability = mutableListOf<BikeStationWithAvailability>()

        for ((station_id, availability) in availabilityByStationId) {
            val station = stationsByStationId.get(station_id)
            station?.let {
                stationsWithAvailability.add(
                    BikeStationWithAvailability(
                        station_id,
                        station.name,
                        availability.num_bikes_available,
                        availability.num_docks_available
                    )
                )
            }
                ?: run {
                    // Handle the case when despite the update to stations info it's still mismatched
                    // Perhaps we shouldn't even show it if we have no info about the station itself?
                    // Depends how important it is to show partial data vs throw an error if just one
                    // station is incomplete
                    stationsWithAvailability.add(
                        BikeStationWithAvailability(
                            station_id,
                            "Unknown station",
                            availability.num_bikes_available,
                            availability.num_docks_available
                        )
                    )
                }
        }

        // TODO Sort the list to have a predictable order
        return stationsWithAvailability
    }

    /** Combine Oslo Bysykkel base API url with the path to various resources */
    fun getGBFSUrl(path: String): String {
        return UriComponentsBuilder.fromHttpUrl(gbfsBaseUrl).path(path).toUriString()
    }

    /** Download JSON data with information about bike stations with their names, addresses, etc. */
    override fun getStationsInfoData(url: String): StationsInfoJSON {
        // TODO Better handling of errors from the external API
        val stationsInfo = get(url).jsonObject.toString()
        val stationsInfoJson = gson.fromJson(stationsInfo, StationsInfoJSON::class.java)
        return stationsInfoJson
    }

    /**
     * Download JSON data with status information for each bike station, including number of available
     * bikes, number of empty dockins stations, etc.
     */
    override fun getStationsStatusData(url: String): StationsStatusJSON {
        // TODO Better handling of errors from the external API
        val statusInfo = get(url).jsonObject.toString()
        val statusInfoJson = gson.fromJson(statusInfo, StationsStatusJSON::class.java)
        return statusInfoJson
    }
}
