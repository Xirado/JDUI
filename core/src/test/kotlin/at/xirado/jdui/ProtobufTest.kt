package at.xirado.jdui

import at.xirado.jdui.config.Secret
import at.xirado.jdui.crypto.decryptChaCha
import at.xirado.jdui.crypto.encryptChaCha
import at.xirado.jdui.utils.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class ProtobufTest {
    @Test
    @OptIn(ExperimentalSerializationApi::class)
    fun testSerialization() {
        @Serializable
        data class Foo(val word: String)
        @Serializable
        data class Bar(val id: Int)

        val foo = Foo("Hello, World!")
        val bar = Bar(1234567)

        val fooSer = ProtoBuf.encodeToByteArray(foo)
        val barSer = ProtoBuf.encodeToByteArray(bar)

        val packed = packProtoMessages(listOf(fooSer, barSer))

        val password = "verysecretpassword123"
        val salt = hexStringToByteArray("deadbeefdeadbeef0123012301234567")
        val secret = Secret(password, salt)

        val nonce = hexStringToByteArray("deadbeef")
        val encrypted = encryptChaCha(packed, secret, nonce)

        val encoded = encode(encrypted)

        val decoded = decode(encoded)
        val decrypted = decryptChaCha(decoded, secret, nonce)
        val unpacked = unpackProtoMessages(decrypted)

        val fooDeser = ProtoBuf.decodeFromByteArray<Foo>(unpacked[0])
        val barDeser = ProtoBuf.decodeFromByteArray<Bar>(unpacked[1])

        assertEquals(foo, fooDeser)
        assertEquals(bar, barDeser)
    }
}