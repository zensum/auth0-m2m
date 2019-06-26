package io.klira.auth0.m2m

interface TokenService {
    suspend fun requestToken(): String
}