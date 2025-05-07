package at.xirado.jdui.view.metadata

import at.xirado.jdui.JDUIListener
import at.xirado.jdui.config.Secret
import at.xirado.jdui.crypto.decryptChaCha
import at.xirado.jdui.crypto.encryptChaCha
import at.xirado.jdui.utils.decode
import at.xirado.jdui.utils.toBytes
import at.xirado.jdui.view.metadata.source.ViewSourceData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
internal class ViewStateMetadata(
    val sourceData: ViewSourceData?,
    var userState: ByteArray?,
)

// TODO: Maybe find a better name for these

@Serializable
internal class EncryptedViewStateMetadata(
    val id: Long,
    private val metadataEncrypted: ByteArray,
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun decrypt(secret: Secret): DecryptedViewStateMetadata {
        val decrypted = decryptChaCha(metadataEncrypted, secret, id.toBytes())
        val metadata: ViewStateMetadata = ProtoBuf.decodeFromByteArray(decrypted)

        return DecryptedViewStateMetadata(id, metadata)
    }

    override fun toString(): String {
        return "EncryptedViewStateMetadata(id = $id)"
    }
}

@Serializable
internal class DecryptedViewStateMetadata(
    val id: Long,
    val metadata: ViewStateMetadata,
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun encrypt(secret: Secret): EncryptedViewStateMetadata {
        val serialized = ProtoBuf.encodeToByteArray(metadata)
        val encrypted = encryptChaCha(serialized, secret, id.toBytes())

        return EncryptedViewStateMetadata(id, encrypted)
    }

    override fun toString(): String {
        return "DecryptedViewStateMetadata(id = $id)"
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal fun decodeViewState(encodedData: String): EncryptedViewStateMetadata {
    val decodedData = decode(encodedData)
    return ProtoBuf.decodeFromByteArray(decodedData)
}

@OptIn(ExperimentalSerializationApi::class)
internal suspend fun retrieveViewMetadataFromDb(jdui: JDUIListener, id: Long): EncryptedViewStateMetadata {
    val persistence = jdui.config.persistenceConfig
        ?: throw IllegalStateException("JDUI does not have a PersistenceConfig!")

    val retrievedState = persistence.retrieveState(id)
        ?: throw IllegalStateException("No such view with id $id")

    return ProtoBuf.decodeFromByteArray(retrievedState.data)
}

