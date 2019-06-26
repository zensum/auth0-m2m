package io.klira.auth0.m2m

import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

internal object ClientFactory {
    private const val CACHE_SIZE: Long = 1024 * 128
    private const val MAX_IDLE_CONNECTIONS = 2

    fun createDefaultClient(connectionPool: ConnectionPool = defaultConnectionPool()): OkHttpClient {
        val cache = Cache(createTemporaryCacheDirectory(), CACHE_SIZE)
        cache.initialize()

        return OkHttpClient()
            .newBuilder()
            .cache(cache)
            .callTimeout(Duration.ofSeconds(10L))
            .connectTimeout(Duration.ofSeconds(10L))
            .connectionPool(connectionPool)
            .build()
    }

    private fun defaultConnectionPool(): ConnectionPool =
        ConnectionPool(MAX_IDLE_CONNECTIONS, 60, TimeUnit.SECONDS)

    private fun createTemporaryCacheDirectory(): File {
        val dir: Path = Files.createTempDirectory(UUID.randomUUID().toString())
        return dir.toFile()
    }
}