package com.lukelast.simplefin

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.logging.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

fun defaultHttpClient(logLevel: LogLevel = LogLevel.INFO) =
    HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(false)
                connectTimeout(5, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
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
            socketTimeoutMillis = 60.seconds.inWholeMilliseconds
        }
        install(ContentEncoding) {
            gzip()
            deflate()
        }
        install(Logging) {
            level = logLevel
            logger = Logger.DEFAULT
        }
    }
