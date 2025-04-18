package at.xirado.jdui.view.metadata.source

import at.xirado.jdui.view.definition.ViewDefinition
import kotlinx.serialization.Serializable

internal interface ViewSource {
    val data: ViewSourceData
    fun create(): ViewDefinition
}

@Serializable
sealed interface ViewSourceData {
    val type: Int
}

