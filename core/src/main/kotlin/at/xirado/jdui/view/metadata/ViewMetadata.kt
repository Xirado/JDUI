package at.xirado.jdui.view.metadata

import at.xirado.jdui.view.metadata.source.ViewSourceData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class ViewIdentifier(
    val id: Long,
    val sourceData: ViewSourceData?,
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class ViewMetadata(
    @ProtoNumber(1)
    var viewIdentifier: ViewIdentifier,
    @ProtoNumber(2)
    var userState: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ViewMetadata

        if (viewIdentifier != other.viewIdentifier) return false
        if (!userState.contentEquals(other.userState)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = viewIdentifier.hashCode()
        result = 31 * result + userState.contentHashCode()
        return result
    }

    override fun toString(): String {
        return viewIdentifier.id.toString()
    }
}

