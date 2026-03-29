package com.nexvault.wallet.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds cache control headers for price API responses.
 *
 * Prices don't change every second, so we cache responses for 30 seconds
 * to reduce API calls and improve performance.
 *
 * Ref: doc/09-PERFORMANCE-OPTIMIZATION.md Section 3.2
 */
class CacheControlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return response.newBuilder()
            .header("Cache-Control", "public, max-age=30")
            .build()
    }
}
