package com.nexvault.wallet.feature.onboarding.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.usecase.wallet.GetMnemonicForBackupUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerifyMnemonicViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getMnemonicForBackupUseCase: GetMnemonicForBackupUseCase

    private val testMnemonic = listOf(
        "apple", "brave", "crane", "delta", "eagle", "frost",
        "grape", "house", "ivory", "jump", "king", "lamp"
    )

    @Before
    fun setup() {
        savedStateHandle = mockk(relaxed = true)
        every { savedStateHandle.get<String>("walletId") } returns "test-wallet-id"
        getMnemonicForBackupUseCase = mockk()
    }

    @Test
    fun correctOrder() = runTest {
        coEvery { getMnemonicForBackupUseCase(any()) } returns DataResult.Success(testMnemonic)

        val viewModel = VerifyMnemonicViewModel(savedStateHandle, getMnemonicForBackupUseCase)

        // Wait for loading to complete
        while (viewModel.uiState.value.isLoading) {
            kotlinx.coroutines.delay(10)
        }

        // Tap words in correct order
        for (word in testMnemonic) {
            viewModel.onWordSelected(word)
        }

        assertTrue(viewModel.uiState.value.isVerified)
        assertEquals(12, viewModel.uiState.value.selectedWords.size)
        assertTrue(viewModel.uiState.value.availableWords.isEmpty())
    }

    @Test
    fun incorrectOrder() = runTest {
        coEvery { getMnemonicForBackupUseCase(any()) } returns DataResult.Success(testMnemonic)

        val viewModel = VerifyMnemonicViewModel(savedStateHandle, getMnemonicForBackupUseCase)

        // Wait for loading to complete
        while (viewModel.uiState.value.isLoading) {
            kotlinx.coroutines.delay(10)
        }

        // Tap wrong word first (should be "apple")
        viewModel.onWordSelected("brave")

        assertTrue(viewModel.uiState.value.isError)

        // Wait for reset delay
        kotlinx.coroutines.delay(1600)

        assertFalse(viewModel.uiState.value.isError)
        assertTrue(viewModel.uiState.value.selectedWords.isEmpty())
    }

    @Test
    fun deselectLastWord() = runTest {
        coEvery { getMnemonicForBackupUseCase(any()) } returns DataResult.Success(testMnemonic)

        val viewModel = VerifyMnemonicViewModel(savedStateHandle, getMnemonicForBackupUseCase)

        // Wait for loading to complete
        while (viewModel.uiState.value.isLoading) {
            kotlinx.coroutines.delay(10)
        }

        // Select "apple", "brave"
        viewModel.onWordSelected("apple")
        viewModel.onWordSelected("brave")

        assertEquals(2, viewModel.uiState.value.selectedWords.size)

        // Remove last word "brave"
        viewModel.onSelectedWordRemoved("brave")

        assertEquals(1, viewModel.uiState.value.selectedWords.size)
        assertEquals("apple", viewModel.uiState.value.selectedWords.first())
    }

    @Test
    fun cannotDeselectNonLastWord() = runTest {
        coEvery { getMnemonicForBackupUseCase(any()) } returns DataResult.Success(testMnemonic)

        val viewModel = VerifyMnemonicViewModel(savedStateHandle, getMnemonicForBackupUseCase)

        // Wait for loading to complete
        while (viewModel.uiState.value.isLoading) {
            kotlinx.coroutines.delay(10)
        }

        // Select "apple", "brave"
        viewModel.onWordSelected("apple")
        viewModel.onWordSelected("brave")

        // Try to remove first word "apple" (not last)
        viewModel.onSelectedWordRemoved("apple")

        // Should still have both words
        assertEquals(2, viewModel.uiState.value.selectedWords.size)
    }

    @Test
    fun confirmEmitsNavigationWhenVerified() = runTest {
        coEvery { getMnemonicForBackupUseCase(any()) } returns DataResult.Success(testMnemonic)

        val viewModel = VerifyMnemonicViewModel(savedStateHandle, getMnemonicForBackupUseCase)

        // Wait for loading to complete
        while (viewModel.uiState.value.isLoading) {
            kotlinx.coroutines.delay(10)
        }

        // Select all words in correct order
        for (word in testMnemonic) {
            viewModel.onWordSelected(word)
        }

        viewModel.navigationEvent.test {
            viewModel.onConfirmClicked()
            val event = awaitItem()
            assertTrue(event is VerifyMnemonicViewModel.NavigationEvent.NavigateToSetPin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun confirmDoesNothingWhenNotVerified() = runTest {
        coEvery { getMnemonicForBackupUseCase(any()) } returns DataResult.Success(testMnemonic)

        val viewModel = VerifyMnemonicViewModel(savedStateHandle, getMnemonicForBackupUseCase)

        // Wait for loading to complete
        while (viewModel.uiState.value.isLoading) {
            kotlinx.coroutines.delay(10)
        }

        // Only select one word (not fully verified)
        viewModel.onWordSelected("apple")

        viewModel.navigationEvent.test {
            viewModel.onConfirmClicked()
            expectNoEvents()
        }
    }
}
