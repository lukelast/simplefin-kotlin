package com.lukelast.simplefin

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import java.time.Instant
import kotlin.io.encoding.Base64
import kotlinx.serialization.json.Json.Default.decodeFromString

class SimplefinClient(private val client: HttpClient = defaultHttpClient()) : AutoCloseable {
    override fun close() {
        client.close()
    }

    /**
     * Fetches the access token URL using the provided setup token.
     *
     * @param setupToken A Base64 encoded setup token given by Simplefin bridge.
     * @return An [AccessTokenUrl] containing the user, password, and base URL.
     * @throws SetupTokenUsedException if the setup token has already been used.
     */
    suspend fun fetchAccessUrl(setupToken: String): AccessTokenUrl {
        val requestUrl = Base64.UrlSafe.decode(setupToken).decodeToString()
        val rsp = client.post(requestUrl)
        if (rsp.status == HttpStatusCode.Forbidden) {
            throw SetupTokenUsedException()
        }
        if (rsp.status != OK) {
            throw SimplefinApiException(rsp.status, rsp.bodyAsText(), "fetch access token")
        }
        val rspText = rsp.bodyAsText()
        val rspUrl = AccessTokenUrl(rspText)
        return rspUrl
    }

    suspend fun accounts(
        token: AccessTokenUrl,
        startDate: Instant? = null,
        endDate: Instant? = null,
        pending: Boolean = false,
        accounts: List<String> = listOf(),
        balancesOnly: Boolean = false,
    ): AccountSet {
        val url =
            token
                .accountsUrl()
                .apply {
                    if (startDate != null) {
                        parameters.append("start-date", startDate.epochSecond.toString())
                    }
                    if (endDate != null) {
                        parameters.append("end-date", endDate.epochSecond.toString())
                    }
                    if (pending) {
                        parameters.append("pending", "1")
                    }
                    accounts.forEach { parameters.append("account", it) }
                    if (balancesOnly) {
                        parameters.append("balances-only", "1")
                    }
                }
                .build()
        val rsp =
            client.get(url) {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                basicAuth(token.user, token.pass)
            }
        if (rsp.status != OK) {
            throw SimplefinApiException(rsp.status, rsp.bodyAsText(), "fetch accounts")
        }
        val rspText = rsp.bodyAsText()
        val rspObj: AccountSet = decodeFromString(rspText)
        return rspObj
    }
}
