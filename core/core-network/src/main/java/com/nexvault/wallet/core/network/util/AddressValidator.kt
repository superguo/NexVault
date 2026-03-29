package com.nexvault.wallet.core.network.util

/**
 * Utility for validating Ethereum addresses.
 *
 * Ref: doc/07-TEST-CASES.md TC-NET-002, TC-NET-003
 */
object AddressValidator {
    private val ADDRESS_REGEX = Regex("^0x[0-9a-fA-F]{40}$")

    /**
     * Validates that the given string is a valid Ethereum address.
     *
     * A valid address is:
     * - 42 characters long
     * - Starts with "0x"
     * - Followed by 40 hex characters (0-9, a-f, A-F)
     *
     * This does NOT validate EIP-55 checksum casing.
     *
     * @param address The address string to validate
     * @return true if valid, false otherwise
     */
    fun isValid(address: String): Boolean {
        return ADDRESS_REGEX.matches(address.trim())
    }
}
