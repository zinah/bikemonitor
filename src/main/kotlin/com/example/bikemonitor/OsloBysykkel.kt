package com.example.bikemonitor

import com.google.gson.Gson
import khttp.get
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class OsloBysykkel : GeneralBikeShareFeedSpecification {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val gson = Gson()

  override fun getBikeAvailability(request: ServerRequest): Mono<ServerResponse> {
    // TODO Move these URLs out into some configuration
    val StationsResponse =
        getBikeStationsData(
            "https://gbfs.urbansharing.com/oslobysykkel.no/station_information.json"
        )
    val StationAvailabilityResponse =
        getBikeAvailabilityData("https://gbfs.urbansharing.com/oslobysykkel.no/station_status.json")
    var bikeAvailability = StationAvailabilityResponse.data.stations
    var stationsInfo = StationsResponse.data.stations

    var stationsByStationId = stationsInfo.map { it.station_id to it }.toMap()
    var availabilityByStationId = bikeAvailability.map { it.station_id to it }.toMap()
    val allStationsKnown =
        (stationsByStationId.keys.toSet().containsAll(availabilityByStationId.keys.toSet()))
    if (allStationsKnown != true) {
      TODO("Not implemented yet, need to update stations before proceeding")
    }

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

    var jsonResponse = gson.toJson(stationsWithAvailability)
    return ServerResponse.ok().body(Mono.just(jsonResponse), String::class.java)
  }

  override fun getBikeStationsData(url: String): BikeStationsJSON {
    val fakeStationsJSON =
        """
    {
        "last_updated": 1553592653,
        "data": {
          "stations": [
            {  
              "station_id":"627",
              "name":"Skøyen Stasjon",
              "address":"Skøyen Stasjon",
              "lat":59.9226729,
              "lon":10.6788129,
              "capacity":20
            },
            {  
              "station_id":"623",
              "name":"7 Juni Plassen",
              "address":"7 Juni Plassen",
              "lat":59.9150596,
              "lon":10.7312715,
              "capacity":15
            },
            {  
              "station_id":"610",
              "name":"Sotahjørnet",
              "address":"Sotahjørnet",
              "lat":59.9099822,
              "lon":10.7914482,
              "capacity":20
            }
          ]
        }
      }
    """
    val stations = get(url).jsonObject.toString()
    val stationsJson = gson.fromJson(stations, BikeStationsJSON::class.java)

    val fakeStations = gson.fromJson(fakeStationsJSON, BikeStationsJSON::class.java)
    if (stationsJson == null) {
      return fakeStations
    } else {
      return stationsJson
    }
  }

  override fun getBikeAvailabilityData(url: String): BikeAvailabilityJSON {
    var fakeAvailabilityJSON =
        """
        {
            "last_updated": 1540219230,
            "data": {
              "stations": [
                {
                  "is_installed": 1,
                  "is_renting": 1,
                  "num_bikes_available": 4,
                  "num_docks_available": 8,
                  "last_reported": 1540219230,
                  "is_returning": 1,
                  "station_id": "623"
                },
                {
                  "is_installed": 1,
                  "is_renting": 1,
                  "num_bikes_available": 7,
                  "num_docks_available": 5,
                  "last_reported": 1540219230,
                  "is_returning": 1,
                  "station_id": "627"
                },
                {
                  "is_installed": 1,
                  "is_renting": 1,
                  "num_bikes_available": 4,
                  "num_docks_available": 9,
                  "last_reported": 1540219230,
                  "is_returning": 1,
                  "station_id": "610"
                }
              ]
            }
          }
        """
    val availability = get(url).jsonObject.toString()
    val availabilityJson = gson.fromJson(availability, BikeAvailabilityJSON::class.java)
    val fakeAvailability = gson.fromJson(fakeAvailabilityJSON, BikeAvailabilityJSON::class.java)
    if (availabilityJson == null) {
      return fakeAvailability
    } else {
      return availabilityJson
    }
  }
}
