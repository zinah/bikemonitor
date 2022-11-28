package com.example.bikemonitor

import com.google.gson.Gson
import java.time.Instant
import khttp.get
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Component
class OsloBysykkel : GeneralBikeShareFeedSpecification {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val gson = Gson()
  // TODO Move this out into some sort of configuration
  override val gbfsBaseUrl = "https://gbfs.urbansharing.com/oslobysykkel.no/"

  override fun getBikeAvailability(request: ServerRequest): Mono<ServerResponse> {
    val StationsResponse = getStationsInfoData(getGBFSUrl("station_information.json"))
    val StationAvailabilityResponse = getStationsStatusData(getGBFSUrl("station_status.json"))

    var stationsByStationId = StationsResponse.data.stations.map { it.station_id to it }.toMap()
    var availabilityByStationId =
        StationAvailabilityResponse.data.stations.map { it.station_id to it }.toMap()
    // Do we have info on all stations in the status response?
    val allStationsKnown =
        (stationsByStationId.keys.toSet().containsAll(availabilityByStationId.keys.toSet()))
    if (allStationsKnown != true) {
      // TODO Should we download the station information again?
      // Throwing an error might be an overkill if it's only some stations that are missing info,
      // not all. Depends what the client needs and how strict we should be.
      TODO("Not implemented yet")
    }

    val stationsWithAvailability =
        getStationsWithAvaliability(stationsByStationId, availabilityByStationId)

    val responseBody =
        StationsWithAvailabilityJSON(
            last_updated = Instant.now().epochSecond,
            stations = stationsWithAvailability
        )
    return ServerResponse.ok().body(Mono.just(responseBody))
  }

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

    return stationsWithAvailability
  }

  fun getGBFSUrl(path: String): String {
    return UriComponentsBuilder.fromHttpUrl(gbfsBaseUrl).path(path).toUriString()
  }

  override fun getStationsInfoData(url: String): StationsInfoJSON {
    val stationsInfo = get(url).jsonObject.toString()
    val stationsInfoJson = gson.fromJson(stationsInfo, StationsInfoJSON::class.java)
    return stationsInfoJson
  }

  override fun getStationsStatusData(url: String): StationsStatusJSON {
    val statusInfo = get(url).jsonObject.toString()
    val statusInfoJson = gson.fromJson(statusInfo, StationsStatusJSON::class.java)
    return statusInfoJson
  }
}
