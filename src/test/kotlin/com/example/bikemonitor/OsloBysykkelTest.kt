package com.example.bikemonitor

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeAll
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

        private val stationsInfoApiResponse: String? =
                        this::class.java
                                        .classLoader
                                        .getResource("station_information.json")
                                        ?.readText()
        private val stationsStatusApiResponse: String? =
                        this::class.java.classLoader.getResource("station_status.json")?.readText()

        private val stationsInfoURL =
                        "https://gbfs.urbansharing.com/oslobysykkel.no/station_information.json"
        private val stationsStatusURL =
                        "https://gbfs.urbansharing.com/oslobysykkel.no/station_status.json"

        @BeforeAll
        fun setup() {
                mockkStatic("khttp.KHttp")
                // TODO This should be mocking `khttp.get(stationsInfoURL)` and return Response
                every { khttp.get(stationsInfoURL).jsonObject.toString() } returns
                                stationsInfoApiResponse!!
                // TODO This should be mocking `khttp.get(stationsStatusURL)` and return Response
                every { khttp.get(stationsStatusURL).jsonObject.toString() } returns
                                stationsStatusApiResponse!!
        }

        @Test
        fun `test gbfs station info api response is loaded`() {

                logger.info(stationsInfoApiResponse)
                assert(stationsInfoApiResponse != null)
        }

        @Test
        fun `test gbfs station status api response is loaded`() {

                logger.info(stationsStatusApiResponse)
                assert(stationsStatusApiResponse != null)
        }

        @Test
        fun `test getStationsInfoData OK`() {
                val expectedStations =
                                listOf(
                                                StationInfo(
                                                                station_id = "627",
                                                                name = "Skøyen Stasjon",
                                                                address = "Skøyen Stasjon",
                                                                lat = 59.9226729,
                                                                lon = 10.6788129,
                                                                capacity = 20
                                                ),
                                                StationInfo(
                                                                station_id = "623",
                                                                name = "7 Juni Plassen",
                                                                address = "7 Juni Plassen",
                                                                lat = 59.9150596,
                                                                lon = 10.7312715,
                                                                capacity = 15
                                                ),
                                                StationInfo(
                                                                station_id = "610",
                                                                name = "Sotahjørnet",
                                                                address = "Sotahjørnet",
                                                                lat = 59.9099822,
                                                                lon = 10.7914482,
                                                                capacity = 20
                                                )
                                )

                assert(
                                osloBysykkel.getStationsInfoData(stationsInfoURL).last_updated ==
                                                1553592653.toLong()
                )
                assert(
                                osloBysykkel.getStationsInfoData(stationsInfoURL)
                                                .data
                                                .stations
                                                .toSet() == expectedStations.toSet()
                )
        }

        @Test
        fun `test getStationsStatusData OK`() {
                var expectedStatus =
                                listOf(
                                                StationStatus(
                                                                station_id = "623",
                                                                is_installed = 1,
                                                                is_renting = 1,
                                                                num_bikes_available = 4,
                                                                num_docks_available = 8,
                                                                last_reported = 1540219230,
                                                                is_returning = 1
                                                ),
                                                StationStatus(
                                                                station_id = "627",
                                                                is_installed = 1,
                                                                is_renting = 1,
                                                                num_bikes_available = 7,
                                                                num_docks_available = 5,
                                                                last_reported = 1540219230,
                                                                is_returning = 1
                                                ),
                                                StationStatus(
                                                                station_id = "610",
                                                                is_installed = 1,
                                                                is_renting = 1,
                                                                num_bikes_available = 4,
                                                                num_docks_available = 9,
                                                                last_reported = 1540219230,
                                                                is_returning = 1
                                                )
                                )
                assert(
                                osloBysykkel.getStationsStatusData(stationsStatusURL)
                                                .last_updated == 1540219230.toLong()
                )
                assert(
                                osloBysykkel.getStationsStatusData(stationsStatusURL)
                                                .data
                                                .stations
                                                .toSet() == expectedStatus.toSet()
                )
        }
}
