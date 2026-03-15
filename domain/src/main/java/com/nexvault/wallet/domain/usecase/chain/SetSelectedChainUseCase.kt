package com.nexvault.wallet.domain.usecase.chain

import com.nexvault.wallet.domain.model.chain.SupportedChains
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.common.NexVaultException
import com.nexvault.wallet.domain.repository.ChainRepository
import javax.inject.Inject

/**
 * Changes the selected blockchain network.
 */
class SetSelectedChainUseCase @Inject constructor(
    private val chainRepository: ChainRepository,
) {
    suspend operator fun invoke(chainId: Int): DataResult<Unit> {
        if (SupportedChains.getByChainId(chainId) == null) {
            return DataResult.Error(
                NexVaultException("Unsupported chain ID: $chainId"),
            )
        }
        return chainRepository.setSelectedChain(chainId)
    }
}
