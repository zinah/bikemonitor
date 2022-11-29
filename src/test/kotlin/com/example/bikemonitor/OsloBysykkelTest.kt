package com.example.bikemonitor

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        assertNotNull(stationsInfoApiResponse)
    }

    @Test
    fun `test gbfs station status api response is loaded`() {

        logger.info(stationsStatusApiResponse)
        assertNotNull(stationsStatusApiResponse)
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

        assertEquals(
            osloBysykkel.getStationsInfoData(stationsInfoURL).last_updated,
            1553592653.toLong()
        )
        assertEquals(
            osloBysykkel.getStationsInfoData(stationsInfoURL)
                .data
                .stations
                .toSet(),
            expectedStations.toSet()
        )
    }

    @Test
    fun `test getStationsStatusData OK`() {
        val expectedStatus =
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
        assertEquals(
            osloBysykkel.getStationsStatusData(stationsStatusURL)
                .last_updated,
            1540219230.toLong()
        )
        assertEquals(
            osloBysykkel.getStationsStatusData(stationsStatusURL)
                .data
                .stations
                .toSet(),
            expectedStatus.toSet()
        )
    }
}

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetStationsWithAvaliabilityTest {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val osloBysykkel = OsloBysykkel()
    private val skoyenInfo = StationInfo(
        station_id = "627",
        name = "Skøyen Stasjon",
        address = "Skøyen Stasjon",
        lat = 59.9226729,
        lon = 10.6788129,
        capacity = 20
    )
    private val skoyenStatus = StationStatus(
        station_id = "627",
        is_installed = 1,
        is_renting = 1,
        num_bikes_available = 4,
        num_docks_available = 8,
        last_reported = 1540219230,
        is_returning = 1
    )
    private val skoyenStationAvailability = BikeStationWithAvailability(
        station_id = "627",
        name = "Skøyen Stasjon",
        num_bikes_available = 4,
        num_docks_available = 8
    )
    private val sotahjornetInfo = StationInfo(
        station_id = "610",
        name = "Sotahjørnet",
        address = "Sotahjørnet",
        lat = 59.9099822,
        lon = 10.7914482,
        capacity = 20
    )
    private val sotahjornetStatus = StationStatus(
        station_id = "610",
        is_installed = 1,
        is_renting = 1,
        num_bikes_available = 4,
        num_docks_available = 9,
        last_reported = 1540219230,
        is_returning = 1
    )
    private val sotahjornetAvailability = BikeStationWithAvailability(
        station_id = "610",
        name = "Sotahjørnet",
        num_bikes_available = 4,
        num_docks_available = 9
    )
    private val unknowStationAvailability = BikeStationWithAvailability(
        station_id = "627",
        name = "Unknown station",
        num_bikes_available = 4,
        num_docks_available = 8
    )

    @Test
    fun `test all stations with status are included in the result`() {
        val stationsInfoGrouped =
            mapOf(
                sotahjornetInfo.station_id to sotahjornetInfo,
                skoyenInfo.station_id to skoyenInfo
            )
        val statusInfoGrouped =
            mapOf(
                sotahjornetInfo.station_id to sotahjornetStatus,
                skoyenInfo.station_id to skoyenStatus
            )
        val result = osloBysykkel.getStationsWithAvaliability(stationsInfoGrouped, statusInfoGrouped)
        assertEquals(
            result,
            listOf(
                sotahjornetAvailability,
                skoyenStationAvailability
            )
        )
    }

    @Test
    fun `test only stations with status are included in the result`() {
        val stationsInfoGrouped =
            mapOf(
                sotahjornetInfo.station_id to sotahjornetInfo,
                skoyenInfo.station_id to skoyenInfo
            )
        val statusInfoGrouped =
            mapOf(
                sotahjornetInfo.station_id to sotahjornetStatus
            )
        val result = osloBysykkel.getStationsWithAvaliability(stationsInfoGrouped, statusInfoGrouped)
        assertEquals(
            result,
            listOf(
                sotahjornetAvailability
            )
        )
    }

    @Test
    fun `test unknown station is included in the result`() {
        val stationsInfoGrouped =
            mapOf(
                sotahjornetInfo.station_id to sotahjornetInfo
            )
        val statusInfoGrouped =
            mapOf(
                skoyenInfo.station_id to skoyenStatus,
                sotahjornetInfo.station_id to sotahjornetStatus
            )
        val result = osloBysykkel.getStationsWithAvaliability(stationsInfoGrouped, statusInfoGrouped)
        assertEquals(
            result,
            listOf(
                unknowStationAvailability,
                sotahjornetAvailability
            )
        )
    }
}
