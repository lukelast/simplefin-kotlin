package com.lukelast.simplefin


import com.lukelast.simplefin.SimplefinClient
import com.lukelast.simplefin.demoAccessUrl
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

class SimplefinClientTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun accounts() {
        runBlocking {
            val client = SimplefinClient(demoAccessUrl, HttpClient())
            val accounts = client.accounts(
                startDate = Instant.now().minus(50.days.toJavaDuration()),
                endDate = Instant.now(),
                accounts = listOf("Demo Checking"),
                balancesOnly = false
            )
            println(accounts)
            val jsonStr = json.encodeToString(accounts)
            println(jsonStr)
        }
    }
}