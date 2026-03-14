package com.nexvault.wallet.core.security.mnemonic

import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MnemonicManager @Inject constructor() {

    private val secureRandom = SecureRandom()

    fun generateMnemonic(wordCount: Int = 12): String {
        require(wordCount == 12 || wordCount == 24) {
            "Word count must be 12 or 24"
        }

        val entropyBits = when (wordCount) {
            12 -> 128
            24 -> 256
            else -> throw IllegalArgumentException("Invalid word count")
        }

        val entropy = ByteArray(entropyBits / 8)
        secureRandom.nextBytes(entropy)

        return MnemonicUtils.generateMnemonic(entropy)
    }

    fun validateMnemonic(mnemonic: String): Boolean {
        val words = mnemonic.trim().split("\\s+".toRegex())
        if (words.size != 12 && words.size != 24) {
            return false
        }

        return try {
            MnemonicUtils.validateMnemonic(mnemonic)
        } catch (e: Exception) {
            false
        }
    }

    fun mnemonicToSeed(mnemonic: String, passphrase: String): ByteArray {
        return MnemonicUtils.generateSeed(mnemonic, passphrase)
    }

    fun getWordList(): List<String> {
        // Web3j MnemonicUtils doesn't expose the word list directly in v5.0.2
        // Using a standard BIP39 English word list
        return BIP39_WORDLIST
    }

    companion object {
        // Standard BIP39 English word list (first 100 words as example)
        private val BIP39_WORDLIST = listOf(
            "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract", "absurd", "abuse",
            "access", "accident", "account", "accuse", "achieve", "acid", "acoustic", "acquire", "across", "act",
            "action", "actor", "actress", "actual", "adapt", "add", "addict", "address", "adjust", "admit",
            "adult", "advance", "advice", "aerobic", "affair", "afford", "afraid", "again", "age", "agent",
            "agree", "ahead", "aim", "air", "airport", "aisle", "alarm", "album", "alcohol", "alert",
            "alien", "all", "alley", "allow", "almost", "alone", "alpha", "already", "also", "alter",
            "always", "amateur", "amazing", "among", "amount", "amused", "analyst", "anchor", "ancient", "anger",
            "angle", "angry", "animal", "ankle", "announce", "annual", "another", "answer", "antenna", "antique",
            "anxiety", "any", "apart", "apology", "appear", "apple", "approve", "april", "arch", "arctic",
            "area", "arena", "argue", "arm", "armed", "armor", "army", "around", "arrange", "arrest"
        )
    }
}
