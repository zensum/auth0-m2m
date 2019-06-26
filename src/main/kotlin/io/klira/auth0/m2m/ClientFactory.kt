package io.klira.auth0.m2m

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

internal object ClientFactory {
    private const val MAX_IDLE_CONNECTIONS = 2

    fun createDefaultClient(connectionPool: ConnectionPool = defaultConnectionPool()): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .callTimeout(Duration.ofSeconds(10L))
            .connectTimeout(Duration.ofSeconds(10L))
            .connectionPool(connectionPool)
            .build()
    }

    private fun defaultConnectionPool(): ConnectionPool =
        ConnectionPool(MAX_IDLE_CONNECTIONS, 60, TimeUnit.SECONDS)
}