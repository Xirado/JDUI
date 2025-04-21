package at.xirado.jdui.crypto

import at.xirado.jdui.config.Secret
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.ChaCha20ParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal fun deriveKey(secret: Secret): ByteArray {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec = PBEKeySpec(secret.password.toCharArray(), secret.salt, 100000, 256)
    val key = factory.generateSecret(spec)
    return key.encoded
}

internal fun encryptChaCha(plaintext: ByteArray, secret: Secret, nonce: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("ChaCha20")
    val key: SecretKey = SecretKeySpec(secret.derivedKey, "ChaCha20")

    val sha256 = MessageDigest.getInstance("SHA-256")
    val nonceHashed = sha256.digest(nonce).copyOfRange(0, 12)
    val spec = ChaCha20ParameterSpec(nonceHashed, 0)

    cipher.init(Cipher.ENCRYPT_MODE, key, spec)
    val ciphertext = cipher.doFinal(plaintext)

    return ciphertext
}

internal fun decryptChaCha(cipherText: ByteArray, secret: Secret, nonce: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("ChaCha20")
    val key: SecretKey = SecretKeySpec(secret.derivedKey, "ChaCha20")

    val sha256 = MessageDigest.getInstance("SHA-256")
    val nonceHashed = sha256.digest(nonce).copyOfRange(0, 12)
    val spec = ChaCha20ParameterSpec(nonceHashed, 0)

    cipher.init(Cipher.DECRYPT_MODE, key, spec)
    val ciphertext = cipher.doFinal(cipherText)

    return ciphertext
}