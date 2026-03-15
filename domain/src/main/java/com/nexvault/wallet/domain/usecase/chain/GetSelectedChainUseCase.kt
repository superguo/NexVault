package com.nexvault.wallet.domain.usecase.chain

import com.nexvault.wallet.domain.model.chain.Chain
import com.nexvault.wallet.domain.repository.ChainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the currently selected blockchain network.
 */
class GetSelectedChainUseCase @Inject constructor(
    private val chainRepository: ChainRepository,
) {
    operator fun invoke(): Flow<Chain> {
        return chainRepository.getSelectedChain()
    }
}
