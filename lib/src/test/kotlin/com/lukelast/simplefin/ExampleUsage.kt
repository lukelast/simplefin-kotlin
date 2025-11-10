package com.lukelast.simplefin

import io.ktor.client.plugins.logging.LogLevel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

const val user = "demo"
const val pass = "demo"
val accessUrl = AccessTokenUrl(user, pass)
private val json = Json { prettyPrint = true }
private const val SIMPLE_LOGGER_LEVEL_PROPERTY = "org.slf4j.simpleLogger.defaultLogLevel"

object FetchAccessTokenUrl {
    @JvmStatic
    fun main(args: Array<String>) = useClient {
        val setupToken = "your_setup_token_here"
        val token: AccessTokenUrl = fetchAccessUrl(setupToken)
        println(token)
    }
}

object FetchAccounts {
    @JvmStatic
    fun main(args: Array<String>) = useClient {
        val accounts =
            accounts(
                accessUrl,
                startDate = Instant.now().minus(90.days.toJavaDuration()),
                endDate = Instant.now().minus(0.days.toJavaDuration()),
                accounts = listOf(),
                pending = true,
                balancesOnly = false,
            )
        val jsonStr = json.encodeToString(accounts)
        println(jsonStr)
    }
}

private fun useClient(block: suspend SimplefinClient.() -> Unit) {
    configureSimpleLogger()
    runBlocking {
        SimplefinClient(defaultHttpClient(LogLevel.INFO)).use { client -> println("Took ${measureTime { client.block() }}") }
    }
}

private fun configureSimpleLogger() {
    if (System.getProperty(SIMPLE_LOGGER_LEVEL_PROPERTY).isNullOrBlank()) {
        System.setProperty(SIMPLE_LOGGER_LEVEL_PROPERTY, "debug")
    }
}
