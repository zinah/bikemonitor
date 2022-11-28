package com.example.bikemonitor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class BikeMonitorApplication

fun main(args: Array<String>) {
    runApplication<BikeMonitorApplication>(*args)
}
