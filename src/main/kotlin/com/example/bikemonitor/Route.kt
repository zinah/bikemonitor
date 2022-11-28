package com.example.bikemonitor

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

@Configuration
class Route(private val gbfs: GeneralBikeShareFeedSpecification) {

    @Bean
    fun buildRoute() = router {
        ("/stations" and accept(APPLICATION_JSON)).nest {
            GET("/availability", gbfs::getBikeAvailability)
        }
    }
}
