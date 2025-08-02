package com.lukelast.simplefin

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlin.io.encoding.Base64

private fun defaultClient() = HttpClient(CIO) {}

class SimplefinAuthClient(private val client: HttpClient = defaultClient()) : AutoCloseable {
  override fun close() {
    client.close()
  }

  suspend fun fetchAccessUrl(setupToken: String): AccessTokenUrl {
    val requestUrl = Base64.Default.UrlSafe.decode(setupToken).decodeToString()
    val rsp = client.post(requestUrl)
    if (rsp.status == HttpStatusCode.Companion.Forbidden) {
      error("Setup token already used")
    }
    if (rsp.status != HttpStatusCode.Companion.OK) {
      error("Failed to fetch access token")
    }
    val rspText = rsp.readRawBytes().decodeToString()
    val rspUrl = AccessTokenUrl(rspText)
    return rspUrl
  }
}
