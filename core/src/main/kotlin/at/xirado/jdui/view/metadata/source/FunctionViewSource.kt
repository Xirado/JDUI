package at.xirado.jdui.view.metadata.source

import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.definition.function.ViewDefinitionFunction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.kotlinFunction

@Serializable
@SerialName("0")
internal data class FunctionViewSourceData(
    val className: String,
    val functionName: String,
) : ViewSourceData {
    override val type = 0
}

internal class FunctionViewSource(
    private val _data: FunctionViewSourceData
): ViewSource {
    private val function = findFunction()

    override fun create(): ViewDefinition {
        return function.callBy(emptyMap())
    }

    @Suppress("UNCHECKED_CAST")
    private fun findFunction(): KFunction<ViewDefinitionFunction> {
        val clazz = Class.forName(data.className)

        val method = clazz.getMethod(data.functionName)

        val function = method.kotlinFunction
            ?: throw IllegalStateException("Method cannot be represented as a kotlin function! ($data)")

        val returnType = function.returnType
        val returnTypeClass = returnType.classifier
            ?: throw IllegalStateException("Function must return ViewDefinitionFunction! ($data)")
        returnTypeClass as KClass<*>

        if (!returnTypeClass.isSubclassOf(ViewDefinitionFunction::class))
            throw IllegalStateException("Function must return ViewDefinitionFunction! ($data)")

        return function as KFunction<ViewDefinitionFunction>
    }

    override val data: FunctionViewSourceData
        get() = _data
}