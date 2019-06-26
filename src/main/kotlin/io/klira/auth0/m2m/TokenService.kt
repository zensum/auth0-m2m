package io.klira.auth0.m2m

import java.time.Duration

interface TokenService {

    /**
     * Retrieve an authentication token.
     *
     * If a token is cachee, use it if
     * 1. It is no older than [maxAge] *and*
     * 2. The time until the token expires is equal to
     * or greater than [expirationThreshold]
     *
     * If a token is not cached, a new one will be fetched
     */
    suspend fun requestToken(
        maxAge: Duration = defaultMaxAge,
        expirationThreshold: Duration = defaultThreshold
    ): String

    companion object {
        private val defaultMaxAge = Duration.ofMinutes(30L)
        private val defaultThreshold = Duration.ofMinutes(20L)
    }
}