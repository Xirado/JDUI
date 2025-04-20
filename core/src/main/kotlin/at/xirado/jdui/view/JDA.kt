package at.xirado.jdui.view

import at.xirado.jdui.Context
import at.xirado.jdui.getInstance
import at.xirado.jdui.state.createViewState
import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.definition.function.ViewDefinitionFunction
import at.xirado.jdui.view.metadata.MessageContext
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.RestAction
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

suspend fun IReplyCallback.replyView(
    function: KFunction<ViewDefinitionFunction>,
    ephemeral: Boolean = false,
    context: Context? = null,
): RestAction<InteractionHook> {
    val state = createFunctionViewState(jda, function, context)
    val messageContext = state.messageContext

    messageContext.provideInteractionHook(hook)
    val message = state.composeMessage()
    return reply(message).setEphemeral(ephemeral).populateMessageContext(messageContext)
}

suspend inline fun <reified T: View> IReplyCallback.replyView(
    ephemeral: Boolean = false,
    context: Context? = null,
) = replyView(T::class, ephemeral, context)

suspend fun <T: View> IReplyCallback.replyView(
    clazz: KClass<T>,
    ephemeral: Boolean = false,
    context: Context? = null,
): RestAction<InteractionHook> {
    val state = createClassViewState(jda, clazz, context)
    val messageContext = state.messageContext

    messageContext.provideInteractionHook(hook)
    val message = state.composeMessage()
    return reply(message).setEphemeral(ephemeral).populateMessageContext(messageContext)
}

suspend fun IReplyCallback.replyView(
    definition: ViewDefinition,
    ephemeral: Boolean = false,
    context: Context? = null,
): RestAction<InteractionHook> {
    val instance = getInstance(jda)
    val state = createViewState(instance, definition, context = context)
    val messageContext = state.messageContext

    messageContext.provideInteractionHook(hook)

    val message = state.composeMessage()
    return reply(message).setEphemeral(ephemeral).populateMessageContext(messageContext)
}

suspend fun MessageChannel.sendView(
    function: KFunction<ViewDefinitionFunction>,
    context: Context? = null,
): RestAction<Message> {
    val state = createFunctionViewState(jda, function, context)
    val messageContext = state.messageContext

    val message = state.composeMessage()
    return sendMessage(message).populateMessageContext(messageContext)
}

suspend inline fun <reified T: View> MessageChannel.sendView(context: Context? = null) = sendView(T::class, context)

suspend fun <T: View> MessageChannel.sendView(clazz: KClass<T>, context: Context? = null): RestAction<Message> {
    val state = createClassViewState(jda, clazz, context)
    val messageContext = state.messageContext

    val message = state.composeMessage()
    return sendMessage(message).populateMessageContext(messageContext)
}

suspend fun MessageChannel.sendView(definition: ViewDefinition, context: Context? = null): RestAction<Message> {
    val instance = getInstance(jda)
    val state = createViewState(instance, definition, context = context)
    val messageContext = state.messageContext

    val message = state.composeMessage()
    return sendMessage(message).populateMessageContext(messageContext)
}

@JvmName("populateMessageContextInteractionHook")
internal fun RestAction<InteractionHook>.populateMessageContext(context: MessageContext) = onSuccess {
    context.provideInteractionHook(it)
}

@JvmName("populateMessageContextInteractionMessage")
private fun RestAction<Message>.populateMessageContext(context: MessageContext) = onSuccess {
    context.provideMessageSource(it.channelIdLong, it.idLong)
}