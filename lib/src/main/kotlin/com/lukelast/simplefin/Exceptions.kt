package com.lukelast.simplefin

import io.ktor.http.*

/** Base exception for all SimpleFIN API errors. */
sealed class SimplefinException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/** Thrown when an API request fails with a non-200 status code. */
class SimplefinApiException(
    val status: HttpStatusCode,
    val responseBody: String,
    endpoint: String,
) : SimplefinException("Failed to $endpoint: $status, $responseBody")

/** Thrown when a setup token has already been used and cannot be reused. */
class SetupTokenUsedException : SimplefinException("Setup token already used")

/** Thrown when an access token URL is invalid or malformed. */
class InvalidAccessUrlException(message: String) : SimplefinException(message)
