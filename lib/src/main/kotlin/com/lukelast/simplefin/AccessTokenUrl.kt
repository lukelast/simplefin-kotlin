package com.lukelast.simplefin

import io.ktor.http.*

private const val URL = "beta-bridge.simplefin.org/simplefin"

val demoAccessUrl = AccessTokenUrl("https://demo:demo@$URL")

class AccessTokenUrl private constructor(val user: String, val pass: String, private val url: Url) {
    constructor(user: String, pass: String) : this(user, pass, Url("https://$URL"))

    fun accountsUrl(): URLBuilder = URLBuilder(url).appendPathSegments("accounts")

    val fullUrlString =
        URLBuilder(url)
            .apply {
                this@apply.user = this@AccessTokenUrl.user
                this@apply.password = this@AccessTokenUrl.pass
            }
            .buildString()

    override fun toString(): String = fullUrlString

    companion object {
        operator fun invoke(urlText: String): AccessTokenUrl {
            val url = Url(urlText)
            if (url.protocol != URLProtocol.HTTPS) {
                throw InvalidAccessUrlException("Access URL must use HTTPS protocol")
            }
            if (!url.host.contains("simplefin")) {
                throw InvalidAccessUrlException("Access URL must contain 'simplefin' in the host")
            }
            if (url.segments != listOf("simplefin")) {
                throw InvalidAccessUrlException("Access URL must have path '/simplefin'")
            }
            val user = url.user ?: throw InvalidAccessUrlException("Missing username in access URL")
            val pass = url.password ?: throw InvalidAccessUrlException("Missing password in access URL")
            val updatedUrl =
                URLBuilder(url)
                    .apply {
                        this.user = null
                        this.password = null
                    }
                    .build()
            return AccessTokenUrl(user, pass, updatedUrl)
        }
    }
}
