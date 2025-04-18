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
    fun <T> getUserState(property: UserStateProperty<T>): T {
        property as UserStateProperty<Any?>
        val index = property.index

        if (!userState.containsKey(property)) {
            if (index > unpackedUserState.lastIndex) {
                userState[property] = property.default
                return property.default
            }

            val serialized = unpackedUserState[index]
            val deserialized = if (serialized.isNotEmpty())
                ProtoBuf.decodeFromByteArray(property.serializer, serialized)
            else
                null

            userState[property] = deserialized
            return deserialized as T
        }

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
                if (value != null)
                    ProtoBuf.encodeToByteArray(it.key.serializer, it.value)
                else
                    ByteArray(0)
            }

        return packProtoMessages(serialized)
    }
}