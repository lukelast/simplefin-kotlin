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
            require(url.protocol == URLProtocol.HTTPS)
            require(url.host.contains("simplefin"))
            require(url.segments == listOf("simplefin"))
            val user = url.user ?: error("Missing username")
            val pass = url.password ?: error("Missing password")
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
