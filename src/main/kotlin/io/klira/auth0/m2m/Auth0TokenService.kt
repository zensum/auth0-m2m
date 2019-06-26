package io.klira.auth0.m2m

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.time.Duration
import java.util.concurrent.CompletableFuture

class Auth0TokenService(
    private val client: OkHttpClient = ClientFactory.createDefaultClient(),
    audience: String = System.getenv().getOrDefault("AUTH0_AUDIENCE", DEFAULT_AUDIENCE),
    clientId: String = System.getenv("AUTH0_CLIENT_ID"),
    clientSecret: String = System.getenv("AUTH0_CLIENT_SECRET")
): TokenService {
    private val requestBody = TokenRequest(clientId, clientSecret, audience)
    private val log = KotlinLogging.logger("Auth0 Client [$audience]")
    private val token: Atomic<Token> = Atomic()

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

    override suspend fun requestToken(maxAge: Duration, expirationThreshold: Duration): String {
        log.debug { "Requesting Auth0 M2M token" }
         val tokenFound: Token = token.updateAndGet { current: Token? ->
             try {
                 when {
                     current == null -> fetchToken()
                     current.isExpired(expirationThreshold) -> fetchToken()
                     current.age() > maxAge -> fetchToken()
                     else -> current.also { log.debug { "Using saved token" } }
                 }
             } catch(e: Throwable) {
                 log.error { "Unable to fetch token: ${e.message}" }
                 current ?: error("Token was required, but none was present and fetch failed")
             }
        }

        return tokenFound.token
    }

    private suspend fun fetchToken(): Token {
        val request: Request = buildRequest()
        val tokenContent: CompletableFuture<String> = client.asyncRequest(request, transform = ::handleResponse)
        return Token(tokenContent.await())
    }

    private fun buildRequest(): Request {
        val json: String = jacksonObjectMapper().writeValueAsString(requestBody)
        val body: RequestBody = RequestBody.create(MediaType.get("application/json"), json)

        return Request.Builder()
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
