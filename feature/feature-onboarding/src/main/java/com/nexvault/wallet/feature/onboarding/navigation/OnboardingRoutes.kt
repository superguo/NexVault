package com.nexvault.wallet.feature.onboarding.navigation

/**
 * Navigation route constants for the onboarding flow.
 */
object OnboardingRoutes {
    const val ONBOARDING_GRAPH = "onboarding_graph"
    const val WELCOME = "onboarding/welcome"
    const val CREATE_WALLET = "onboarding/create_wallet"
    const val VERIFY_MNEMONIC = "onboarding/verify_mnemonic/{walletId}"
    const val IMPORT_WALLET = "onboarding/import_wallet"
    const val SET_PIN = "onboarding/set_pin"

    fun verifyMnemonic(walletId: String): String =
        "onboarding/verify_mnemonic/$walletId"
}
