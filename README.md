# auth0-m2m.library
Wrapper library for requesting a machine-to-machine token from Auth0

[![CircleCI](https://circleci.com/gh/zensum/auth0-m2m.library.svg?style=svg)](https://circleci.com/gh/zensum/auth0-m2m.library)
[![Release](https://jitpack.io/v/zensum/auth0-m2m.library.svg)](https://jitpack.io/#zensum/auth0-m2m.library)

## Motivation
Many services that we have created are exposed to the internet, and requests to
them may have to be authenticated by some means. Our most common authentication
method is by a _bearer token_ that is a JSON Web Token (JWT) which is issued by
[Auth0](https://auth0.com). When our services are making requests to each other
by an Auth0 JWT, a machine-to-machine token is required. This library is a
convenient wrapper for doing requests for fetching such token from Auth0.

See [Using JSON Web Tokens](https://github.com/zensum/documentation/tree/master/security/Auth0/Using%20JSON%20Web%20Tokens.md) for more information on this topic.

## Usage

Add to `build.gradle`:
```gradle
implementation("com.github.zensum:auth0-m2m.library:e64517c")
implementation("com.squareup.okhttp3:okhttp:3.14.2")
```

Minimal example for using [Auth0TokenService](src/main/kotlin/io/klira/auth0/m2m/Auth0TokenService.kt) and doing a request.
```kotlin
import kotlinx.coroutines.runBlocking

fun main() {
    val service: TokenService = Auth0TokenService()
    runBlocking {
        val token: String = service.requestToken()
        println(token)
    }
}
```

All requests are cached for a considerable time, so doing multiple requests
within a short time span should not be an issue. For more information, see code
and documentation in [Auth0TokenService](src/main/kotlin/io/klira/auth0/m2m/Auth0TokenService.kt) class source code.

## Environment Variables
None of the environment variables are required if an argument for each property is
given to the constructor of [Auth0TokenService](src/main/kotlin/io/klira/auth0/m2m/Auth0TokenService.kt) instead.

| Environment Variable |  Description |
| -------------------- | ------------ |
| AUTH0_AUDIENCE       | Sets the audience for the token |
| AUTH0_CLIENT_ID      | Client id used in authentication for token request to Auth0 API |
| AUTH0_CLIENT_SECRET  | Client secret used in authentication for token request to Auth0 API |
| AUTH0_TENANT_URL     | Auth0 URL from which the tokens will be requested, will usually be in the format `https://TENANT.REGION.auth0.com/oauth/token`, unless a custom domain is used. See [Auth0 documentation](https://auth0.com/docs/api/authentication?http#client-credentials-flow) for more information. 
