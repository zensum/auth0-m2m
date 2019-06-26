package io.klira.auth0.m2m

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Duration
import java.time.Instant

data class Token(
    val token: String
) {
    val expires: Instant = expirationFromToken(token)

    fun isExpired(threshold: Duration = Duration.ofMinutes(30L)): Boolean =
        timeUntilExpires().minus(threshold).isNegative

    fun timeUntilExpires(): Duration = Duration.between(Instant.now(), expires)

    companion object {
        private fun expirationFromToken(token: String): Instant {
            val decode: DecodedJWT = JWT.decode(token)
            return decode.expiresAt.toInstant()
        }
    }
}