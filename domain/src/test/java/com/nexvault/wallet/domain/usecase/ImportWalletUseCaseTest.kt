package com.nexvault.wallet.domain.usecase

import com.nexvault.wallet.domain.model.auth.WalletCreationResult
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.common.InvalidMnemonicException
import com.nexvault.wallet.domain.model.common.InvalidPrivateKeyException
import com.nexvault.wallet.domain.repository.WalletRepository
import com.nexvault.wallet.domain.usecase.wallet.ImportWalletUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ImportWalletUseCaseTest {
    private lateinit var walletRepository: WalletRepository
    private lateinit var useCase: ImportWalletUseCase

    @Before
    fun setup() {
        walletRepository = mockk()
        useCase = ImportWalletUseCase(walletRepository)
    }

    @Test
    fun testFromMnemonicWithValid12WordsCallsRepository() = runTest {
        val mnemonic = "abandon " + "about " + "absent " + "abstract " + "absent " +
            "abstract " + "absent " + "abstract " + "absent " + "abstract " +
            "absent " + "absent"
        coEvery {
            walletRepository.importFromMnemonic(any(), any())
        } returns DataResult.Success(
            WalletCreationResult(
                walletId = "id",
                address = "0x123",
                mnemonicWords = emptyList(),
            )
        )

        val result = useCase.fromMnemonic(mnemonic, "Test Wallet")

        assertTrue(result is DataResult.Success)
        coVerify { walletRepository.importFromMnemonic(mnemonic.lowercase(), "Test Wallet") }
    }

    @Test
    fun testFromMnemonicWith10WordsReturnsError() = runTest {
        val mnemonic = "abandon " + "about " + "absent " + "abstract " + "absent " +
            "abstract " + "absent " + "abstract " + "absent " + "about"

        val result = useCase.fromMnemonic(mnemonic)

        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertTrue(error.exception is InvalidMnemonicException)
    }

    @Test
    fun testFromMnemonicWith24WordsCallsRepository() = runTest {
        val mnemonic = List(24) { "word$it" }.joinToString(" ")
        coEvery {
            walletRepository.importFromMnemonic(any(), any())
        } returns DataResult.Success(
            WalletCreationResult(
                walletId = "id",
                address = "0x123",
                mnemonicWords = emptyList(),
            )
        )

        val result = useCase.fromMnemonic(mnemonic)

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun testFromPrivateKeyWithValid64HexCallsRepository() = runTest {
        val privateKey = "a".repeat(64)
        coEvery {
            walletRepository.importFromPrivateKey(any(), any())
        } returns DataResult.Success(
            WalletCreationResult(
                walletId = "id",
                address = "0x123",
                mnemonicWords = emptyList(),
            )
        )

        val result = useCase.fromPrivateKey(privateKey)

        assertTrue(result is DataResult.Success)
        coVerify { walletRepository.importFromPrivateKey(privateKey, "Imported Wallet") }
    }

    @Test
    fun testFromPrivateKeyWith0xPrefixStripsPrefix() = runTest {
        val privateKey = "0x" + "a".repeat(64)
        coEvery {
            walletRepository.importFromPrivateKey(any(), any())
        } returns DataResult.Success(
            WalletCreationResult(
                walletId = "id",
                address = "0x123",
                mnemonicWords = emptyList(),
            )
        )

        val result = useCase.fromPrivateKey(privateKey)

        assertTrue(result is DataResult.Success)
        coVerify { walletRepository.importFromPrivateKey("a".repeat(64), "Imported Wallet") }
    }

    @Test
    fun testFromPrivateKeyWithInvalidLengthReturnsError() = runTest {
        val privateKey = "a".repeat(63)

        val result = useCase.fromPrivateKey(privateKey)

        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertTrue(error.exception is InvalidPrivateKeyException)
    }

    @Test
    fun testFromPrivateKeyWithNonHexCharsReturnsError() = runTest {
        val privateKey = "g".repeat(64)

        val result = useCase.fromPrivateKey(privateKey)

        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertTrue(error.exception is InvalidPrivateKeyException)
    }
}
