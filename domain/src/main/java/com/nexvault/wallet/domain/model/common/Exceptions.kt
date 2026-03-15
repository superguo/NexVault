package com.nexvault.wallet.domain.model.common

/**
 * Domain-specific exceptions.
 */
open class NexVaultException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class InvalidMnemonicException(
    message: String = "Invalid mnemonic phrase",
) : NexVaultException(message)

class InvalidPrivateKeyException(
    message: String = "Invalid private key",
) : NexVaultException(message)

class InvalidAddressException(
    message: String = "Invalid Ethereum address",
) : NexVaultException(message)

class WalletNotFoundException(
    message: String = "Wallet not found",
) : NexVaultException(message)

class WalletAlreadyExistsException(
    message: String = "Wallet already exists",
) : NexVaultException(message)

class InsufficientBalanceException(
    val available: String,
    val required: String,
) : NexVaultException("Insufficient balance: available=$available, required=$required")

class TransactionFailedException(
    message: String,
    cause: Throwable? = null,
) : NexVaultException(message, cause)

class AuthenticationException(
    message: String = "Authentication failed",
) : NexVaultException(message)

class NetworkException(
    message: String = "Network error",
    cause: Throwable? = null,
) : NexVaultException(message, cause)

class EncryptionException(
    message: String = "Encryption error",
    cause: Throwable? = null,
) : NexVaultException(message, cause)
