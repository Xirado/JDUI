package at.xirado.jdui.config

import at.xirado.jdui.crypto.deriveKey

/**
 * Secret used for encrypting view state for storage on Discords' servers.
 *
 * This ensures it cannot be reverse engineered by analyzing the components.
 *
 * **Warning: Changing these values will render components created prior to the change unusable!**
 *
 * @param password Password used for encryption. Should be random and at least 12 characters long
 */
class Secret(val password: String) {
    init {
        require(password.length >= 12) { "Password should have at least 12 characters" }
    }

    internal val derivedKey: ByteArray by lazy {
        deriveKey(this)
    }
}
