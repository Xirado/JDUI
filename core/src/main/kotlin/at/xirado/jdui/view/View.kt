package at.xirado.jdui.view

import at.xirado.jdui.Context
import at.xirado.jdui.component.ViewContainer
import at.xirado.jdui.getInstance
import at.xirado.jdui.state.ViewState
import at.xirado.jdui.state.createViewState
import at.xirado.jdui.view.definition.clazz.ViewDefinitionClass
import at.xirado.jdui.view.definition.function.ComposeFunction
import at.xirado.jdui.view.definition.function.ViewDefinitionFunction
import at.xirado.jdui.view.metadata.source.ClassViewSourceData
import at.xirado.jdui.view.metadata.source.FunctionViewSourceData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.JDA
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaMethod

private val definitionThreadLocal = ThreadLocal<ViewDefinitionClass>()

abstract class View {
    private var definition: ViewDefinitionClass? = definitionThreadLocal.get()
    private val isInitialized: Boolean
        get() = definition != null

    val coroutineScope: CoroutineScope
        get() = withDefinition { it.coroutineScope }

    val context: Context
        get() = withDefinition { it.context }

    suspend fun <T> locked(block: suspend () -> T): T {
        return withDefinition {
            it.state.mutex.withLock { block() }
        }
    }

    fun triggerMessageUpdate() {
        withDefinition { it.triggerMessageUpdate() }
    }

    open suspend fun initialize() {

    }

    abstract suspend fun createView(): ViewContainer

    /**
     * Returns true if the View was initialized, false if it already was
     */
    internal fun tryInitialize(definition: ViewDefinitionClass): Boolean {
        if (isInitialized) {
            if (this.definition !== definition)
                throw IllegalStateException("View was already initialized with a different definition!")
            return false
        }

        this.definition = definition
        return true
    }

    internal inline fun <T> withDefinition(block: (state: ViewDefinitionClass) -> T): T {
        return definition?.let(block)
            ?: throw IllegalStateException("Action requires View to be initialized!")
    }
}

internal fun <T: View> KClass<out T>.instantiateViewClass(definition: ViewDefinitionClass): T {
    return try {
        definitionThreadLocal.set(definition)
        val constructor = primaryConstructor
            ?: throw IllegalStateException("Class does not have a constructor")

        constructor.callBy(emptyMap())
    } finally {
        definitionThreadLocal.remove()
    }
}

suspend inline fun compose(block: ComposeFunction): ViewContainer {
    val container = ViewContainer()
    block(container)
    return container
}

internal suspend fun <T: View> createClassViewState(
    jda: JDA,
    clazz: KClass<T>,
    context: Context?,
): ViewState {
    val instance = getInstance(jda)
    val name = clazz.qualifiedName
        ?: throw IllegalStateException("Class does not have a name")

    val sourceData = ClassViewSourceData(name)
    return createViewState(instance, sourceData, context = context)
}

internal suspend fun createFunctionViewState(
    jda: JDA,
    function: KFunction<ViewDefinitionFunction>,
    context: Context?,
): ViewState {
    val instance = getInstance(jda)

    val clazz = function.javaMethod?.declaringClass
        ?: throw IllegalStateException("Function must be representable as java method")

    val sourceData = FunctionViewSourceData(clazz.name, function.name)
    return createViewState(instance, sourceData, context = context)
}