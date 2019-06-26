package io.klira.auth0.m2m

import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

internal object ClientFactory {
    fun createDefaultClient(connectionPool: ConnectionPool = defaultConnectionPool()): OkHttpClient {
        val cache = Cache(File("/var/tmp/okhttp"), 40960)

        return OkHttpClient()
            .newBuilder()
            .cache(cache)
            .callTimeout(Duration.ofSeconds(10L))
            .connectTimeout(Duration.ofSeconds(10L))
            .connectionPool(connectionPool)
            .build()
    }

    private fun defaultConnectionPool(): ConnectionPool = ConnectionPool(
        2, 60, TimeUnit.SECONDS
    )
}