package com.lukelast.simplefin

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.io.encoding.Base64

private fun defaultClient() = HttpClient(CIO) {}

class SimplefinAuthClient(private val client: HttpClient = defaultClient()) : AutoCloseable {
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
        if (rsp.status != HttpStatusCode.OK) {
            error("Failed to fetch access token")
        }
        val rspText = rsp.readRawBytes().decodeToString()
        val rspUrl = AccessTokenUrl(rspText)
        return rspUrl
    }
}

class SetupTokenUsedException : Exception("Setup token already used")
