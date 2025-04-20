package at.xirado.jdui.view

import at.xirado.jdui.Context
import at.xirado.jdui.getInstance
import at.xirado.jdui.state.createViewState
import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.definition.function.ViewDefinitionFunction
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

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