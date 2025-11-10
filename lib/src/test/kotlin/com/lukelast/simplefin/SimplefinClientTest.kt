package com.lukelast.simplefin

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
            SimplefinClient().use { client ->
                val accounts =
                    client.accounts(
                        demoAccessUrl,
                        startDate = Instant.now().minus(50.days.toJavaDuration()),
                        endDate = Instant.now(),
                        accounts = listOf("Demo Checking"),
                        balancesOnly = false,
                    )
                println(accounts)
                val jsonStr = json.encodeToString(accounts)
                println(jsonStr)
            }
        }
    }
}
