package at.xirado.jdui.state

import at.xirado.jdui.utils.packProtoMessages
import at.xirado.jdui.utils.unpackProtoMessages
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

internal class UserStateCollection(
    userState: ByteArray?,
) {
    private val unpackedUserState = unpackProtoMessages(userState)
    private val userState: MutableMap<UserStateProperty<Any?>, Any?> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalSerializationApi::class)
    internal fun <T> tryInitProperty(property: UserStateProperty<T>) {
        property as UserStateProperty<Any?>
        val index = property.index

        if (userState.containsKey(property))
            return

        if (index > unpackedUserState.lastIndex) {
            userState[property] = property.default()
            return
        }

        val serialized = unpackedUserState[index]
        val deserialized = serialized?.let { ProtoBuf.decodeFromByteArray(property.serializer, serialized) }

        userState[property] = deserialized
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getUserState(property: UserStateProperty<T>): T {
        property as UserStateProperty<Any?>
        tryInitProperty(property)

        return userState[property] as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> setUserState(property: UserStateProperty<T>, value: T) {
        property as UserStateProperty<Any?>
        userState[property] = value
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun serializeAndPackUserData(): ByteArray {
        val serialized = userState.entries
            .sortedBy { it.key.index }
            .map {
                val value = it.value
                value?.let { value -> ProtoBuf.encodeToByteArray(it.key.serializer, value) }
            }

        return packProtoMessages(serialized)
    }
}