package io.klira.auth0.m2m

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class ServiceConfigTest {

    @Test
    fun `do not allow blank values in config`() {
        val config = ServiceConfig(mapOf("AUTH0_AUDIENCE" to ""))
        assertThrows<IllegalArgumentException> {
            config.audience()
        }
    }

    @Test
    fun `do not allow http for Auth0 tenant`() {
        val config = ServiceConfig(mapOf("AUTH0_TENANT" to "http://my-tenant.auth0.com/oauth/token"))
        assertThrows<IllegalArgumentException> {
            config.tenant()
        }
    }
}