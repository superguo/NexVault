package com.nexvault.wallet.data.mapper

import com.nexvault.wallet.core.database.entity.TokenEntity
import com.nexvault.wallet.domain.model.token.Token
import java.math.BigDecimal

/**
 * Maps a [TokenEntity] row to the domain [Token] model.
 */
fun TokenEntity.toDomain(): Token = Token(
    contractAddress = contractAddress,
    chainId = chainId,
    symbol = symbol,
    name = name,
    decimals = decimals,
    logoUrl = logoUrl,
    balance = BigDecimal(balance),
    fiatPrice = fiatPrice,
    fiatValue = fiatValue,
    priceChange24h = priceChange24h,
    coinGeckoId = coinGeckoId,
    isCustom = isCustom,
)
