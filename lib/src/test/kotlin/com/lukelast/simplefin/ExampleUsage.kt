package com.lukelast.simplefin

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
    runBlocking {
        SimplefinClient().use { client -> println("Took ${measureTime { client.block() }}") }
    }
}
