package at.xirado.jdui.config

import at.xirado.jdui.crypto.deriveKey

/**
 * Secret used for encrypting view state for storage on Discords' servers.
 *
 * This ensures it cannot be reverse engineered by analyzing the components.
 *
 * **Warning: Changing any of these values will render components created prior to the change unusable!**
 *
 * @param password Password used for encryption. Should be random and at least 16 characters long
 * @param salt     Salt used for encryption. Should be 16 or 32 bytes long. Use SecureRandom to generate one. Can be hardcoded safely
 */
class Secret(val password: String, val salt: ByteArray) {
    init {
        require(password.length >= 16) { "Password should have at least 16 characters" }
        require(salt.size == 16 || salt.size == 32) { "Salt can only be 16 or 32 bytes long" }
    }

    internal val derivedKey: ByteArray by lazy {
        deriveKey(this)
    }
}
