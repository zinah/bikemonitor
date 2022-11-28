package com.example.bikemonitor

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

interface GeneralBikeShareFeedSpecification {
    val gbfsBaseUrl: String
    fun getBikeAvailability(request: ServerRequest): Mono<ServerResponse>
    fun getStationsInfoData(url: String): StationsInfoJSON
    fun getStationsStatusData(url: String): StationsStatusJSON
}
