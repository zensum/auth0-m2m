package io.klira.auth0.m2m

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.future.await
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Auth0TokenService(
    private val client: Call.Factory = ClientFactory.createDefaultClient(),
    audience: String = System.getenv().getOrDefault("AUTH0_AUDIENCE", DEFAULT_AUDIENCE),
    clientId: String = System.getenv("AUTH0_CLIENT_ID"),
    clientSecret: String = System.getenv("AUTH0_CLIENT_SECRET")
): TokenService {
    private val requestBody = TokenRequest(clientId, clientSecret, audience)

    /**
     * We should use caching since the expiration time of JWTs from
     * Auth0 is usually quite long, and thus, reduce the amount
     * of actual requests to Auth0
     */
    private val cacheControl = CacheControl.Builder()
        .maxAge(1, TimeUnit.HOURS)
        .minFresh(30, TimeUnit.MINUTES)
        .build()

    /**
     * Secondary constructor which takes a [ConnectionPool] as an
     * argument rather than a [Call.Factory]. This is
     * useful if an existing OkHttp client exists, but you do not want
     * to reuse the client but still have a shared connection pool.
     */
    constructor(
        connectionPool: ConnectionPool,
        audience: String = System.getenv().getOrDefault("AUTH0_AUDIENCE", DEFAULT_AUDIENCE),
        clientId: String = System.getenv("AUTH0_CLIENT_ID"),
        clientSecret: String = System.getenv("AUTH0_CLIENT_SECRET")
    ): this(ClientFactory.createDefaultClient(connectionPool), audience, clientId, clientSecret)

    override suspend fun requestToken(): String {
        val request: Request = buildRequest()
        val token: CompletableFuture<String> = client.asyncRequest(request, transform = ::handleResponse)
        return token.await()
    }

    private fun buildRequest(): Request {
        val json: String = jacksonObjectMapper().writeValueAsString(requestBody)
        val body: RequestBody = RequestBody.create(MediaType.get("application/json"), json)

        return Request.Builder()
            .cacheControl(cacheControl)
            .url(AUTH0_TOKEN_ENDPOINT)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
    }

    private fun handleResponse(call: Call, response: Response): String {
        val responseBody: String? = response.body()?.string()
        if(response.code() >= 400) {
            error("Unexpected response (${response.code()}): $responseBody")
        }
        val data: Map<String, String> = jacksonObjectMapper().readValue(responseBody!!)
        return data["access_token"] ?: error("Missing field 'access_token' in response, got $data")
    }

    companion object {
        const val DEFAULT_AUDIENCE = "https://DEFAULT_AUDIENCE"
        private const val AUTH0_TOKEN_ENDPOINT = "https://TENANT.REGION.auth0.com/oauth/token"
    }
}
