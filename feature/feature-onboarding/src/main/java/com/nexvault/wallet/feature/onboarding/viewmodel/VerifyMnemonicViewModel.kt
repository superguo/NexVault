package com.nexvault.wallet.feature.onboarding.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.usecase.wallet.GetMnemonicForBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Verify Mnemonic screen.
 *
 * Receives the walletId via SavedStateHandle (navigation argument).
 * Loads the mnemonic from the repository, shuffles a copy for verification.
 * User taps words in the correct order. If all words are selected correctly,
 * the Confirm button becomes enabled.
 *
 * On incorrect selection, shows an error state with shake animation trigger,
 * then resets after a short delay.
 */
@HiltViewModel
class VerifyMnemonicViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMnemonicForBackupUseCase: GetMnemonicForBackupUseCase,
) : ViewModel() {

    // Constructor for testing purposes
    constructor(
        savedStateHandle: SavedStateHandle,
        getMnemonicForBackupUseCase: GetMnemonicForBackupUseCase,
        dummy: Unit,
    ) : this(savedStateHandle, getMnemonicForBackupUseCase)

    data class UiState(
        val originalWords: List<String> = emptyList(),
        val shuffledWords: List<String> = emptyList(),
        val selectedWords: List<String> = emptyList(),
        val availableWords: List<String> = emptyList(),
        val isVerified: Boolean = false,
        val isError: Boolean = false,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface NavigationEvent {
        data object NavigateToSetPin : NavigationEvent
    }

    private val walletId: String = savedStateHandle.get<String>("walletId") ?: ""

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadMnemonic()
    }

    private fun loadMnemonic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = getMnemonicForBackupUseCase(walletId)

            when (result) {
                is DataResult.Success -> {
                    val words = result.data
                    val shuffled = words.shuffled()
                    _uiState.update {
                        it.copy(
                            originalWords = words,
                            shuffledWords = shuffled,
                            availableWords = shuffled,
                            selectedWords = emptyList(),
                            isLoading = false,
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to load mnemonic",
                        )
                    }
                }
            }
        }
    }

    fun onWordSelected(word: String) {
        val currentState = _uiState.value
        if (currentState.isVerified || currentState.isError) return

        val nextExpectedIndex = currentState.selectedWords.size
        val expectedWord = currentState.originalWords.getOrNull(nextExpectedIndex) ?: return

        if (word == expectedWord) {
            val newSelected = currentState.selectedWords + word
            val newAvailable = currentState.availableWords.toMutableList().apply {
                remove(word)
            }

            val isComplete = newSelected.size == currentState.originalWords.size

            _uiState.update {
                it.copy(
                    selectedWords = newSelected,
                    availableWords = newAvailable,
                    isVerified = isComplete,
                    isError = false,
                )
            }
        } else {
            _uiState.update { it.copy(isError = true) }

            viewModelScope.launch {
                delay(1500)
                resetVerification()
            }
        }
    }

    fun onSelectedWordRemoved(word: String) {
        val currentState = _uiState.value
        if (currentState.isVerified || currentState.isError) return

        if (currentState.selectedWords.lastOrNull() != word) return

        val newSelected = currentState.selectedWords.dropLast(1)
        val newAvailable = currentState.availableWords + word

        _uiState.update {
            it.copy(
                selectedWords = newSelected,
                availableWords = newAvailable,
                isVerified = false,
            )
        }
    }

    private fun resetVerification() {
        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                selectedWords = emptyList(),
                availableWords = currentState.shuffledWords,
                isVerified = false,
                isError = false,
            )
        }
    }

    fun onConfirmClicked() {
        if (_uiState.value.isVerified) {
            _navigationEvent.tryEmit(NavigationEvent.NavigateToSetPin)
        }
    }

    fun onResetClicked() {
        resetVerification()
    }
}
