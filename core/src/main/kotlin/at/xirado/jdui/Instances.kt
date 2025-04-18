package at.xirado.jdui

import at.xirado.jdui.state.createViewState
import at.xirado.jdui.view.View
import at.xirado.jdui.view.createClassViewState
import at.xirado.jdui.view.createFunctionViewState
import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.definition.function.ViewDefinitionFunction
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

private val instances: MutableMap<JDA, JDUIListener> = IdentityHashMap()
private val lock = ReentrantReadWriteLock()

internal fun appendSession(jda: JDA, jdui: JDUIListener): Unit = lock.write {
    if (jda !in instances) {
        instances[jda] = jdui
        jdui.jda = jda
    }
}

internal fun removeSession(jda: JDA): Unit = lock.write {
    instances.remove(jda)
}

internal fun getInstance(jda: JDA): JDUIListener = lock.read {
    instances[jda]
} ?: throw IllegalStateException("No JDUIListener registered on this JDA instance")

suspend fun IReplyCallback.replyView(
    function: KFunction<ViewDefinitionFunction>,
    ephemeral: Boolean = false,
    context: Context? = null
) {
    val state = createFunctionViewState(jda, function, context)

    state.messageContext.provideInteractionHook(hook)
    val message = state.composeMessage()

    reply(message).setEphemeral(ephemeral).queue()
}

suspend inline fun <reified T: View> IReplyCallback.replyView(ephemeral: Boolean = false, context: Context? = null) {
    return replyView(T::class, ephemeral, context)
}

suspend fun <T: View> IReplyCallback.replyView(clazz: KClass<T>, ephemeral: Boolean = false, context: Context? = null) {
    val state = createClassViewState(jda, clazz, context)
    state.messageContext.provideInteractionHook(hook)

    val message = state.composeMessage()
    reply(message).setEphemeral(ephemeral).queue()
}

suspend fun IReplyCallback.replyView(definition: ViewDefinition, ephemeral: Boolean = false, context: Context? = null) {
    val instance = getInstance(jda)
    val state = createViewState(instance, definition, context = context)

    state.messageContext.provideInteractionHook(hook)

    val message = state.composeMessage()
    reply(message).setEphemeral(ephemeral).queue()
}