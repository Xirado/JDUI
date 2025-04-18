package at.xirado.jdui.view.metadata

import at.xirado.jdui.utils.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.lang.ref.WeakReference

internal data class MessageSource(val channelId: Long, val messageId: Long)
internal data class WebhookMessageSource(val client: WebhookClient<Message>, val messageId: Long)

private val log = KotlinLogging.logger { }

internal class MessageContext(
    private val jda: WeakReference<JDA>
) {
    private var webhook: WebhookMessageSource? = null
    private var interactionHook: InteractionHook? = null
    private var messageSource: MessageSource? = null

    fun provideWebhookClient(webhook: WebhookClient<Message>, messageId: Long) {
        require(webhook !is InteractionHook) { "provideWebhookClient() does not work with InteractionHook! Use provideInteractionHook() instead" }
        this.webhook = WebhookMessageSource(webhook, messageId)
    }

    fun provideInteractionHook(hook: InteractionHook) {
        interactionHook = hook
    }

    fun provideMessageSource(channelId: Long, messageId: Long) {
        messageSource = MessageSource(channelId, messageId)
    }

    suspend fun editMessage(messageData: MessageEditData) {
        val methods = listOf(
            ::editMessageByInteractionHook,
            ::editMessageByWebhook,
            ::editMessageByChannel
        )

        var successful = false
        var error: Throwable? = null

        for (method in methods) {
            try {
                val success = method.invoke(messageData)
                if (success) {
                    successful = true
                    break
                }
            } catch (t: Throwable) {
                if (error == null)
                    error = IllegalStateException("Failed to edit message")
                error.addSuppressed(t)
            }
        }

        if (successful) {
            error?.let {
                log.warn(it) { "Failed to update message with 1 or more methods" }
            }
            return
        }

        error?.let { throw it }

        throw IllegalStateException("No available method to edit the message")
    }

    private suspend fun editMessageByWebhook(
        messageData: MessageEditData,
    ): Boolean {
        val webhook = this.webhook ?: return false
        val (client, id) = webhook

        client.editMessageById(id, messageData).await()
        return true
    }

    private suspend fun editMessageByInteractionHook(
        messageData: MessageEditData,
    ): Boolean {
        val hook = this.interactionHook ?: return false
        if (hook.isExpired)
            return false

        hook.editOriginal(messageData).await()
        return true
    }

    private suspend fun editMessageByChannel(
        messageData: MessageEditData
    ): Boolean {
        val messageSource = this.messageSource ?: return false
        val jda = this.jda.get() ?: return false

        val channel = jda.getChannelById(MessageChannel::class.java, messageSource.channelId)
            ?: return false

        channel.editMessageById(messageSource.messageId, messageData).await()
        return true
    }
}