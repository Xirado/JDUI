package at.xirado.jdui.utils

import java.io.ByteArrayOutputStream

fun unpackProtoMessages(data: ByteArray?): List<ByteArray> {
    if (data == null)
        return emptyList()

    val messages = mutableListOf<ByteArray>()
    var index = 0

    while (index < data.size) {
        val (length, newIndex) = decodeVarint(data, index) ?: break
        index = newIndex

        if (index + length > data.size) {
            throw IllegalArgumentException("Incomplete message detected")
        }

        val message = data.copyOfRange(index, index + length)
        messages.add(message)
        index += length
    }

    return messages
}

fun packProtoMessages(messages: List<ByteArray>): ByteArray {
    val outputStream = ByteArrayOutputStream()

    for (message in messages) {
        val lengthPrefix = encodeVarint(message.size)
        outputStream.write(lengthPrefix)
        outputStream.write(message)
    }

    return outputStream.toByteArray()
}

fun encodeVarint(value: Int): ByteArray {
    require(value >= 0) { "Varint cannot be negative" }

    val output = mutableListOf<Byte>()
    var temp = value

    while (temp > 0x7F) {
        output.add(((temp and 0x7F) or 0x80).toByte())
        temp = temp ushr 7
    }
    output.add(temp.toByte())

    return output.toByteArray()
}

fun decodeVarint(data: ByteArray, startIndex: Int): Pair<Int, Int>? {
    var result = 0
    var shift = 0
    var index = startIndex

    while (index < data.size) {
        val byte = data[index].toInt() and 0xFF
        result = result or ((byte and 0x7F) shl shift)
        index++

        if ((byte and 0x80) == 0) { // MSB is 0 → end of varint
            return Pair(result, index)
        }

        shift += 7
        if (shift >= 32) {
            throw IllegalArgumentException("Varint too long")
        }
    }
    return null
}