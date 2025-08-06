package com.lukelast.simplefin

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

private val json = Json { prettyPrint = true }

fun main() {
    runBlocking {
        val accessUrl = AccessTokenUrl("", "")
        val client = SimplefinClient(accessUrl, HttpClient())
        val accounts =
            client.accounts(
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
