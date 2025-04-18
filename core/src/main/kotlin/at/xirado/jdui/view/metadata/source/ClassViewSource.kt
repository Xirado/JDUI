package at.xirado.jdui.view.metadata.source

import at.xirado.jdui.view.View
import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.definition.clazz.ViewDefinitionClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Serializable
@SerialName("1")
internal data class ClassViewSourceData(val clazzName: String) : ViewSourceData {
    override val type = 1
}

internal class ClassViewSource(
    private val _data: ClassViewSourceData
) : ViewSource {
    private val clazz = findClass()

    override fun create(): ViewDefinition {
        return ViewDefinitionClass(clazz, null)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findClass(): KClass<out View> {
        val clazz = Class.forName(data.clazzName).kotlin

        if (!clazz.isSubclassOf(View::class))
            throw IllegalStateException("Class does not extend View! ($data)")

        return clazz as KClass<out View>
    }

    override val data: ClassViewSourceData
        get() = _data
}

