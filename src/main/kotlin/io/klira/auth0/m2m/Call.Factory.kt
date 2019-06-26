package io.klira.auth0.m2m

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture

/**
 * Use a [Callback] to make an asynchronous request with OkHttp
 * and receive a [CompletableFuture] for type [T] given the
 * transform function [transform].
 */
fun <T> Call.Factory.asyncRequest(
    request: Request,
    future: CompletableFuture<T> = CompletableFuture(),
    transform: (call: Call, response: Response) -> T
): CompletableFuture<T> {
    val callback = object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            future.completeExceptionally(e)
        }

        override fun onResponse(call: Call, response: Response) {
            future.complete(transform(call, response))
        }
    }

    this.newCall(request).enqueue(callback)
    return future
}