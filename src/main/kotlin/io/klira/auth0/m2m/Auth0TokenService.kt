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
import java.lang.RuntimeException
import java.time.Duration
import java.util.concurrent.CompletableFuture

class Auth0TokenService(
    private val client: OkHttpClient = ClientFactory.createDefaultClient(),
    config: ServiceConfig = ServiceConfig()
): TokenService {
    private val requestBody = TokenRequest(config.clientId, config.clientSecret, config.audience)
    private val log = KotlinLogging.logger("Auth0 Client [${config.audience}]")
    private val token: Atomic<Token> = Atomic()

    /**
     * Secondary constructor which takes a [ConnectionPool] as an
     * argument rather than a [Call.Factory]. This is
     * useful if an existing OkHttp client exists, but you do not want
     * to reuse the client but still have a shared connection pool.
     */
    constructor(
        connectionPool: ConnectionPool,
        config: ServiceConfig = ServiceConfig()
    ): this(ClientFactory.createDefaultClient(connectionPool), config)

    constructor(
        client: OkHttpClient = ClientFactory.createDefaultClient(),
        config: Map<String, String>
    ): this(client, ServiceConfig(config))

    override suspend fun requestToken(maxAge: Duration, expirationThreshold: Duration): String {
         val tokenFound: Token = token.updateAndGet { current: Token? ->
             try {
                 when {
                     current == null -> fetchToken()
                     current.isExpired(expirationThreshold) -> fetchToken()
                     current.age() > maxAge -> fetchToken()
                     else -> current.also { log.debug { "Using saved token" } }
                 }
             } catch(exception: Throwable) {
                 log.error { "Unable to fetch token: ${exception.message}" }
                 current ?: fetchFailed(exception)
             }
        }

        return tokenFound.token
    }

    private suspend fun fetchToken(): Token {
        val request: Request = buildRequest()
        log.debug { "Requesting Auth0 M2M token" }
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

    override fun shutdown() {
        this.client.connectionPool().evictAll()
        this.client.dispatcher().cancelAll()
        this.client.dispatcher().executorService().shutdown()
    }

    companion object {
        private const val AUTH0_TOKEN_ENDPOINT = "https://TENANT.auth0.com/oauth/token"
    }
}

const val ERROR_MESSAGE = "Token was required, but none was present and fetch failed"

private fun fetchFailed(exception: Throwable): Nothing {
    throw IllegalStateException(ERROR_MESSAGE, exception)
}
