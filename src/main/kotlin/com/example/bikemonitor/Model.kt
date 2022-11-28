package com.example.bikemonitor

data class BikeStation(
        val station_id: String,
        val name: String,
        val address: String,
        val lat: Double,
        val lon: Double,
        val capacity: Int
)

data class BikeStationAvailability(
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

data class StationsData(val stations: List<BikeStation>)

data class BikeStationsJSON(val last_updated: Int, val data: StationsData)

data class AvailabilityData(val stations: List<BikeStationAvailability>)

data class BikeAvailabilityJSON(var last_updated: Int, var data: AvailabilityData)
