package at.xirado.jdui.view.metadata

import at.xirado.jdui.utils.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.messages.MessageEditData
import okio.withLock
import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantLock

private val log = KotlinLogging.logger { }

internal class MessageContext(
    private val jda: WeakReference<JDA>
) {
    private val lock = ReentrantLock()

    private var webhook: WebhookMessageSource? = null
    private var interactionHook: InteractionHook? = null
    private var messageSource: MessageSource? = null

    fun provideWebhookClient(webhook: WebhookClient<Message>, messageId: Long) {
        require(webhook !is InteractionHook) { "provideWebhookClient() does not work with InteractionHook! Use provideInteractionHook() instead" }
        lock.withLock { this.webhook = WebhookMessageSource(webhook, messageId) }
    }

    fun provideInteractionHook(hook: InteractionHook) = lock.withLock {
        interactionHook = hook

        if (hook.hasCallbackResponse() && messageSource == null) {
            hook.callbackResponse.message?.let {
                provideMessageSource(it.channelIdLong, it.idLong)
            }
        }
    }

    fun provideMessageSource(channelId: Long, messageId: Long) = lock.withLock {
        messageSource = MessageSource(channelId, messageId)
    }

    suspend fun editMessage(messageData: MessageEditData) {
        val restActions = lock.withLock {
            editMethods.mapNotNull { it.invoke(this, messageData) }
        }

        if (restActions.isEmpty())
            throw IllegalStateException("No available method to edit the message")

        val exception = IllegalStateException("Failed to edit message")

        for (action in restActions) {
            try {
                action.await()
                return
            } catch (e: Exception) {
                exception.addSuppressed(e)
            }
        }

        throw exception
    }

    private fun editMessageByInteractionHook(
        messageData: MessageEditData,
    ): RestAction<Message>? {
        val hook = this.interactionHook ?: return null
        if (hook.isExpired)
            return null

        return hook.editOriginal(messageData)
    }

    private fun editMessageByWebhook(
        messageData: MessageEditData,
    ): RestAction<Message>? {
        val webhook = this.webhook ?: return null
        val (client, id) = webhook

        return client.editMessageById(id, messageData)
    }

    private fun editMessageByChannel(
        messageData: MessageEditData
    ): RestAction<Message>? {
        val messageSource = this.messageSource ?: return null
        val jda = this.jda.get() ?: return null

        val channel = jda.getChannelById(MessageChannel::class.java, messageSource.channelId)
            ?: return null

        return channel.editMessageById(messageSource.messageId, messageData)
    }

    private companion object {
        private val editMethods = listOf(
            MessageContext::editMessageByInteractionHook,
            MessageContext::editMessageByWebhook,
            MessageContext::editMessageByChannel
        )
    }
}

internal data class MessageSource(val channelId: Long, val messageId: Long)
internal data class WebhookMessageSource(val client: WebhookClient<Message>, val messageId: Long)