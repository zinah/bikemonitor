package com.example.bikemonitor

data class StationInfo(
                val station_id: String,
                val name: String,
                val address: String,
                val lat: Double,
                val lon: Double,
                val capacity: Int
)

data class StationStatus(
                val station_id: String,
                val is_installed: Int,
                val is_renting: Int,
                val num_bikes_available: Int,
                val num_docks_available: Int,
                val last_reported: Int,
                val is_returning: Int
)

data class BikeStationWithAvailability(
                val station_id: String,
                val name: String,
                val num_bikes_available: Int,
                val num_docks_available: Int
)

data class StationsInfoData(val stations: List<StationInfo>)

data class StationsInfoJSON(val last_updated: Long, val data: StationsInfoData)

data class StationsStatusData(val stations: List<StationStatus>)

data class StationsStatusJSON(val last_updated: Long, val data: StationsStatusData)

data class StationsWithAvailability(val stations: MutableList<BikeStationWithAvailability>)

data class StationsWithAvailabilityJSON(val last_updated: Long, val data: StationsWithAvailability)
