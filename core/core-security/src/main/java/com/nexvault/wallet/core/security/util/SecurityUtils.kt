package com.nexvault.wallet.core.security.util

import java.security.MessageDigest
import java.security.SecureRandom

object SecureUtils {

    fun ByteArray.secureWipe() {
        for (i in indices) {
            this[i] = 0
        }
    }

    fun CharArray.secureWipe() {
        for (i in indices) {
            this[i] = '\u0000'
        }
    }

    fun secureWipeString(value: String) {
        try {
            val charArrayField = value.javaClass.getDeclaredField("value")
            charArrayField.isAccessible = true
            val chars = charArrayField.get(value) as CharArray
            chars.secureWipe()
        } catch (e: Exception) {
            // Best effort - String is immutable in JVM
        }
    }
}

object SecurityUtils {

    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    fun generateSecureRandom(length: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }

    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false

        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    fun ByteArray.toHex(): String {
        val result = StringBuilder(size * 2)
        forEach { byte ->
            val i = byte.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }
        return result.toString()
    }

    fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0) { "Hex string must have even length" }

        val len = length / 2
        val result = ByteArray(len)
        for (i in 0 until len) {
            val high = Character.digit(this[i * 2], 16) shl 4
            val low = Character.digit(this[i * 2 + 1], 16)
            result[i] = (high or low).toByte()
        }
        return result
    }

    fun checksumAddress(address: String): String {
        if (!isValidEthereumAddress(address)) {
            return address
        }

        val cleanAddress = address.removePrefix("0x").lowercase()
        val hash = cleanAddress.toByteArray().sha3Keccak()

        return buildString {
            append("0x")
            for (i in cleanAddress.indices) {
                val hashChar = Character.digit(hash[i / 2], 16)
                if (hashChar and 0x10 != 0 || cleanAddress[i].isUpperCase()) {
                    append(cleanAddress[i].uppercaseChar())
                } else {
                    append(cleanAddress[i])
                }
            }
        }
    }

    fun isValidEthereumAddress(address: String): Boolean {
        if (!address.startsWith("0x")) return false
        if (address.length != 42) return false

        val hexPart = address.substring(2)
        return hexPart.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }

    private fun ByteArray.sha3Keccak(): ByteArray {
        val digest = MessageDigest.getInstance("SHA3-256")
        return digest.digest(this)
    }
}
