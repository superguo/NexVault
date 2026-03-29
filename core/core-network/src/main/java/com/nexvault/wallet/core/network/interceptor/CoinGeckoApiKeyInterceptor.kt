package com.nexvault.wallet.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds CoinGecko API key header if available.
 *
 * The free tier of CoinGecko doesn't require a key, but the Pro tier
 * uses the header "x-cg-demo-api-key". We add it only if a key is provided.
 */
class CoinGeckoApiKeyInterceptor(
    private val apiKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (apiKey.isNotBlank()) {
            chain.request().newBuilder()
                .header("x-cg-demo-api-key", apiKey)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
