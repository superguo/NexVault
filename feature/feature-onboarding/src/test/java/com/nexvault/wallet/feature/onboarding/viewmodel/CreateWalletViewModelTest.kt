package com.nexvault.wallet.feature.onboarding.viewmodel

import app.cash.turbine.test
import com.nexvault.wallet.domain.model.auth.WalletCreationResult
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.usecase.wallet.CreateWalletUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateWalletViewModelTest {

    private lateinit var createWalletUseCase: CreateWalletUseCase
    private lateinit var viewModel: CreateWalletViewModel

    private val testMnemonic = listOf(
        "apple", "brave", "crane", "delta", "eagle", "frost",
        "grape", "house", "ivory", "jump", "king", "lamp"
    )

    @Before
    fun setup() {
        createWalletUseCase = mockk()
    }

    @Test
    fun mnemonicLoadedOnInit() = runTest {
        coEvery { createWalletUseCase(any()) } returns DataResult.Success(
            WalletCreationResult(
                walletId = "abc",
                address = "0x1234...abcd",
                mnemonicWords = testMnemonic,
            )
        )

        viewModel = CreateWalletViewModel(createWalletUseCase)

        val state = viewModel.uiState.value
        assertEquals(12, state.mnemonicWords.size)
        assertEquals("abc", state.walletId)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun creationError() = runTest {
        coEvery { createWalletUseCase(any()) } returns DataResult.Error(
            exception = Exception("Encryption failed"),
            message = "Encryption failed"
        )

        viewModel = CreateWalletViewModel(createWalletUseCase)

        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Encryption failed") == true)
        assertFalse(state.isLoading)
        assertTrue(state.mnemonicWords.isEmpty())
    }

    @Test
    fun acknowledgeTogglesState() = runTest {
        coEvery { createWalletUseCase(any()) } returns DataResult.Success(
            WalletCreationResult(
                walletId = "abc",
                address = "0x1234",
                mnemonicWords = testMnemonic,
            )
        )

        viewModel = CreateWalletViewModel(createWalletUseCase)

        assertFalse(viewModel.uiState.value.isAcknowledged)

        viewModel.onAcknowledgeToggled(true)
        assertTrue(viewModel.uiState.value.isAcknowledged)

        viewModel.onAcknowledgeToggled(false)
        assertFalse(viewModel.uiState.value.isAcknowledged)
    }

    @Test
    fun continueEmitsNavigationEvent() = runTest {
        coEvery { createWalletUseCase(any()) } returns DataResult.Success(
            WalletCreationResult(
                walletId = "abc",
                address = "0x1234",
                mnemonicWords = testMnemonic,
            )
        )

        viewModel = CreateWalletViewModel(createWalletUseCase)

        viewModel.navigationEvent.test {
            viewModel.onContinueClicked()
            val event = awaitItem()
            assertTrue(event is CreateWalletViewModel.NavigationEvent.NavigateToVerifyMnemonic)
            assertEquals("abc", (event as CreateWalletViewModel.NavigationEvent.NavigateToVerifyMnemonic).walletId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun retryAfterErrorCreatesNewWallet() = runTest {
        var callCount = 0
        coEvery { createWalletUseCase(any()) } answers {
            callCount++
            if (callCount == 1) {
                DataResult.Error(exception = Exception("First failure"), message = "First failure")
            } else {
                DataResult.Success(
                    WalletCreationResult(
                        walletId = "retry-id",
                        address = "0x5678",
                        mnemonicWords = testMnemonic,
                    )
                )
            }
        }

        viewModel = CreateWalletViewModel(createWalletUseCase)

        assertTrue(viewModel.uiState.value.error != null)

        viewModel.onRetryClicked()

        assertNull(viewModel.uiState.value.error)
        assertEquals(2, callCount)
    }
}
