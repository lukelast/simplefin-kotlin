package com.lukelast.simplefin

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.OK
import kotlinx.serialization.json.Json.Default.decodeFromString
import java.time.Instant
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


val defaultClient = HttpClient(CIO) {}

class SimplefinAuthClient(private val client: HttpClient = defaultClient) : AutoCloseable {
    override fun close() {
        client.close()
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun fetchAccessUrl(setupToken: String): AccessTokenUrl {
        val requestUrl = Base64.UrlSafe.decode(setupToken).decodeToString()
        val rsp = client.post(requestUrl)
        if (rsp.status == Forbidden) {
            error("Setup token already used")
        }
        if (rsp.status != OK) {
            error("Failed to fetch access token")
        }
        val rspText = rsp.readRawBytes().decodeToString()
        val rspUrl = AccessTokenUrl(rspText)
        return rspUrl
    }
}

class SimplefinClient(private val token: AccessTokenUrl, private val client: HttpClient = defaultClient) :
    AutoCloseable {
    override fun close() {
        client.close()
    }

    suspend fun accounts(
        startDate: Instant? = null,
        endDate: Instant? = null,
        pending: Boolean = false,
        accounts: List<String> = listOf(),
        balancesOnly: Boolean = false,
    ): AccountSet {
        val url = token.accountsUrl().apply {
            if (startDate != null) {
                parameters.append("start-date", startDate.epochSecond.toString())
            }
            if (endDate != null) {
                parameters.append("end-date", endDate.epochSecond.toString())
            }
            if (pending) {
                parameters.append("pending", "1")
            }
            accounts.forEach {
                parameters.append("account", it)
            }
            if (balancesOnly) {
                parameters.append("balances-only", "1")
            }
        }.build()
        val rsp = client.get(url) {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            basicAuth(token.user, token.pass)
        }
        if (rsp.status != OK) {
            error("Failed to fetch accounts")
        }
        val rspText = rsp.bodyAsText()
        val rspObj: AccountSet = decodeFromString(rspText)
        return rspObj
    }
}