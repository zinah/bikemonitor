package com.example.bikemonitor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OsloBysykkelTest {
        private val logger: Logger = LoggerFactory.getLogger(javaClass)
        private val osloBysykkel = OsloBysykkel()

        @Test
        fun `test getBikeStationsData OK`() {
                logger.info(osloBysykkel.getBikeStationsData().toString())
                val expectedStations =
                                listOf(
                                                BikeStation(
                                                                station_id = "627",
                                                                name = "Skøyen Stasjon",
                                                                address = "Skøyen Stasjon",
                                                                lat = 59.9226729,
                                                                lon = 10.6788129,
                                                                capacity = 20
                                                ),
                                                BikeStation(
                                                                station_id = "623",
                                                                name = "7 Juni Plassen",
                                                                address = "7 Juni Plassen",
                                                                lat = 59.9150596,
                                                                lon = 10.7312715,
                                                                capacity = 15
                                                ),
                                                BikeStation(
                                                                station_id = "610",
                                                                name = "Sotahjørnet",
                                                                address = "Sotahjørnet",
                                                                lat = 59.9099822,
                                                                lon = 10.7914482,
                                                                capacity = 20
                                                )
                                )

                assert(osloBysykkel.getBikeStationsData().last_updated == 1553592653)
                assert(
                                osloBysykkel.getBikeStationsData().data.stations.toSet() ==
                                                expectedStations.toSet()
                )
        }

        @Test
        fun `test getBikeAvailability OK`() {
                logger.info(osloBysykkel.getBikeAvailabilityData().toString())
                var expectedAvailability =
                                listOf(
                                                BikeStationAvailability(
                                                                station_id = "623",
                                                                is_installed = 1,
                                                                is_renting = 1,
                                                                num_bikes_available = 4,
                                                                num_docks_available = 8,
                                                                last_reported = 1540219230,
                                                                is_returning = 1
                                                ),
                                                BikeStationAvailability(
                                                                station_id = "627",
                                                                is_installed = 1,
                                                                is_renting = 1,
                                                                num_bikes_available = 7,
                                                                num_docks_available = 5,
                                                                last_reported = 1540219230,
                                                                is_returning = 1
                                                ),
                                                BikeStationAvailability(
                                                                station_id = "610",
                                                                is_installed = 1,
                                                                is_renting = 1,
                                                                num_bikes_available = 4,
                                                                num_docks_available = 9,
                                                                last_reported = 1540219230,
                                                                is_returning = 1
                                                )
                                )
                assert(osloBysykkel.getBikeAvailabilityData().last_updated == 1540219230)
                assert(
                                osloBysykkel.getBikeAvailabilityData().data.stations.toSet() ==
                                                expectedAvailability.toSet()
                )
        }
}
