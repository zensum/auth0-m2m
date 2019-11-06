package io.klira.auth0.m2m

data class ServiceConfig(
    @Transient
    private val config: Map<String, String> = System.getenv()
): Map<String, String> by config {

    fun audience(): String = read(AUDIENCE)
    fun clientId(): String = read(ID)
    fun clientSecret(): String = read(SECRET)
    fun tenant(): String = read(TENANT).also {
        require(it.startsWith("https://")) { "Tenant URL must use https" }
    }

    fun withClientId(clientId: String): ServiceConfig =
        this.copy(config = config.plus(ID to clientId))

    fun withClientSecret(clientSecret: String): ServiceConfig =
        this.copy(config = config.plus(SECRET to clientSecret))

    fun withAudience(audience: String): ServiceConfig =
        this.copy(config = config.plus(AUDIENCE to audience))

    fun withTenant(tenant: String): ServiceConfig {
        require(tenant.startsWith("https://")) { "Tenant URL must use https" }
        return this.copy(config = config.plus(TENANT to tenant))
    }

    private fun read(key: String): String {
        val value: String = config[key] ?: error("Missing value for $key")
        require(value.isNotBlank())
        return value
    }

    companion object {
        private const val AUDIENCE = "AUTH0_AUDIENCE"
        private const val ID = "AUTH0_CLIENT_ID"
        private const val SECRET = "AUTH0_CLIENT_SECRET"
        private const val TENANT = "AUTH0_TENANT"
    }
}
