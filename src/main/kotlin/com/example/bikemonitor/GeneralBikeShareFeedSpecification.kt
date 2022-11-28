package com.example.bikemonitor

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

interface GeneralBikeShareFeedSpecification {
    fun getBikeAvailability(request: ServerRequest): Mono<ServerResponse>
    fun getBikeStationsData(url: String): BikeStationsJSON
    fun getBikeAvailabilityData(url: String): BikeAvailabilityJSON
}
