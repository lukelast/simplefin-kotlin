package com.lukelast.simplefin

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import kotlin.time.Duration.Companion.seconds

fun defaultHttpClient() =
    HttpClient(CIO) {
        engine {
            maxConnectionsCount = 50
            endpoint {
                maxConnectionsPerRoute = 50
                connectAttempts = 2
                keepAliveTime = 60.seconds.inWholeMilliseconds
            }
        }
        install(HttpRequestRetry) {
            maxRetries = 1
            retryOnServerErrors(maxRetries)
            retryOnException(maxRetries, true)
            exponentialDelay()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60.seconds.inWholeMilliseconds
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 45.seconds.inWholeMilliseconds
        }
    }
