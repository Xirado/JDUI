package at.xirado.jdui

import at.xirado.jdui.utils.decode
import at.xirado.jdui.utils.encode
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Base65536Test {
    @Test
    fun testBase65536() {
        val bytes = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)

        val encoded = encode(bytes)
        val decoded = decode(encoded)

        assertContentEquals(bytes, decoded)
    }
}