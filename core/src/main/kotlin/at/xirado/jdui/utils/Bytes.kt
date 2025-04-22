package at.xirado.jdui.utils

import java.nio.ByteBuffer

internal fun Long.toBytes(): ByteArray = ByteBuffer.allocate(8).putLong(this).array()

fun hexStringToByteArray(hex: String): ByteArray =
    hex.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()