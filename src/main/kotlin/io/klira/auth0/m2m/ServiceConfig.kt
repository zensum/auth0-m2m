package io.klira.auth0.m2m

data class ServiceConfig(
    @Transient
    private val config: Map<String, String> = System.getenv()
): Map<String, String> by config {

    val audience: String = config[AUDIENCE] ?: DEFAULT_AUDIENCE
    val clientId: String = config[ID] ?: error("Missing $ID")
    val clientSecret: String = config[SECRET] ?: error("Missing $SECRET")

    fun withClientId(clientId: String): ServiceConfig =
        this.copy(config = config.plus(ID to clientId))

    fun withClientSecret(clientSecret: String): ServiceConfig =
        this.copy(config = config.plus(SECRET to clientSecret))

    fun withAudience(audience: String): ServiceConfig =
        this.copy(config = config.plus(AUDIENCE to audience))

    companion object {
        private const val AUDIENCE = "AUTH0_AUDIENCE"
        private const val ID = "AUTH0_CLIENT_ID"
        private const val SECRET = "AUTH0_CLIENT_SECRET"

        const val DEFAULT_AUDIENCE = "https://RETRACTED"
    }
}
