package io.klira.auth0.m2m

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Model for the body of a request that Auth0 expects when requesting
 * a JWT from their API. Properties [grantType] and [json], will
 * always have the same value in order to make a successful request
 * to Auth0, hence it being non-configurable properties of the class.
 */
internal data class TokenRequest(
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    val audience: String
) {
    @JsonProperty("grant_type")
    val grantType: String = "client_credentials"
    @JsonProperty("json")
    val json: Boolean = true
}