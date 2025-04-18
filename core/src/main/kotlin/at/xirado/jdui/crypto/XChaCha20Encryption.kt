package at.xirado.jdui.crypto

import at.xirado.jdui.config.Secret
import org.bouncycastle.crypto.engines.ChaChaEngine
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import java.nio.charset.StandardCharsets

internal const val KEY_SIZE = 32

private val SALT = intArrayOf(0x8C, 0xD0, 0xA8, 0x61, 0x82, 0x01, 0xDC, 0x8F, 0x59, 0xB9, 0xD9, 0x8D, 0x27, 0x45, 0x5C, 0x0E)
    .map { it.toByte() }
    .toByteArray()

fun deriveKey(secret: Secret): ByteArray {
    val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
        .withSalt(SALT)
        .withIterations(3)
        .withMemoryAsKB(65536)
        .withParallelism(1)
        .build()

    val generator = Argon2BytesGenerator()
    generator.init(params)

    val key = ByteArray(KEY_SIZE)
    generator.generateBytes(secret.password.toByteArray(StandardCharsets.UTF_8), key, 0, key.size)
    return key
}

fun encrypt(plaintext: ByteArray, key: ByteArray): ByteArray {
    val chacha = ChaChaEngine(20)
    val parameters = ParametersWithIV(KeyParameter(key), ByteArray(8) { 0 })
    chacha.init(true, parameters)

    val ciphertext = ByteArray(plaintext.size)
    chacha.processBytes(plaintext, 0, plaintext.size, ciphertext, 0)
    return ciphertext
}

fun decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray {
    val chacha = ChaChaEngine(20)
    val parameters = ParametersWithIV(KeyParameter(key), ByteArray(8) { 0 })
    chacha.init(false, parameters)

    val plaintext = ByteArray(ciphertext.size)
    chacha.processBytes(ciphertext, 0, ciphertext.size, plaintext, 0)
    return plaintext
}