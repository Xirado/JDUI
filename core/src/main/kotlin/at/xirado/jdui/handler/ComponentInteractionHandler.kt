package at.xirado.jdui.handler

import at.xirado.jdui.JDUIListener
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.text
import at.xirado.jdui.crypto.decrypt
import at.xirado.jdui.state.ViewState
import at.xirado.jdui.state.createViewState
import at.xirado.jdui.utils.decode
import at.xirado.jdui.utils.mergeCustomIds
import at.xirado.jdui.view.definition.function.view
import at.xirado.jdui.view.metadata.ViewMetadata
import at.xirado.jdui.view.populateMessageContext
import at.xirado.jdui.view.replyView
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData

private val log = KotlinLogging.logger { }

internal class ComponentInteractionHandler(private val jdui: JDUIListener) {
    private val config = jdui.config

    suspend fun handleComponentEvent(event: GenericComponentInteractionCreateEvent) {
        val id = event.message.components.mergeCustomIds()

        when {
            id.startsWith("j1:") -> handleStatefulView(id, event)
            id.startsWith("j2:") -> handleStatelessView(id, event)
            else -> return
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun handleStatefulView(componentId: String, event: GenericComponentInteractionCreateEvent) {
        val data = componentId.substringAfter("j1:")

        val decodedData = decode(data)
        val secret = config.secret
        val decrypted = decrypt(decodedData, secret.derivedKey)

        val metadata = ProtoBuf.decodeFromByteArray<ViewMetadata>(decrypted)

        val identifier = metadata.viewIdentifier
        val id = identifier.id

        log.debug { "Handling component interaction for view $id" }
        val cachedState = jdui.messageCache.getIfPresent(id)

        if (cachedState != null) {
            val message = updateMessage(event, cachedState)
            return event.editMessage(MessageEditData.fromCreateData(message))
                .populateMessageContext(cachedState.messageContext)
                .queue()
        }

        if (identifier.sourceData == null) {
            return event.replyView(view {
                compose {
                    +container(accentColor = 0xFF0000) {
                        +text("This action timed out!")
                    }
                }
            }, ephemeral = true).queue()
        }

        val state = createViewState(jdui, metadata)

        val message = updateMessage(event, state)
        event.editMessage(MessageEditData.fromCreateData(message))
            .populateMessageContext(state.messageContext)
            .queue()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun handleStatelessView(componentId: String, event: GenericComponentInteractionCreateEvent) {
        val id = componentId.substringAfter("j2:").toLong()
        val cachedState = jdui.messageCache.getIfPresent(id)

        log.debug { "Handling component interaction for view $id" }
        if (cachedState != null) {
            val message = updateMessage(event, cachedState)
            return event.editMessage(MessageEditData.fromCreateData(message))
                .populateMessageContext(cachedState.messageContext)
                .queue()
        }

        log.debug { "Getting state from db: $id" }

        val persistence = jdui.config.persistenceConfig
            ?: throw IllegalStateException("No PersistenceConfig was provided!")

        val retrievedState = persistence.retrieveState(id)
            ?: throw IllegalStateException("No such view with id $id")

        val secret = config.secret
        val decrypted = decrypt(retrievedState.data, secret.derivedKey)

        val metadata = ProtoBuf.decodeFromByteArray<ViewMetadata>(decrypted)

        val identifier = metadata.viewIdentifier

        if (identifier.sourceData == null) {
            return event.replyView(view {
                compose {
                    +container(accentColor = 0xFF0000) {
                        +text("This action timed out!")
                    }
                }
            }, ephemeral = true).queue()
        }

        val state = createViewState(jdui, metadata)
        val message = updateMessage(event, state)

        event.editMessage(MessageEditData.fromCreateData(message))
            .populateMessageContext(state.messageContext)
            .queue()
    }

    private suspend fun updateMessage(event: GenericComponentInteractionCreateEvent, state: ViewState): MessageCreateData {
        return state.mutex.withLock {
            state.handleComponentInteraction(event)
            state.composeMessage()
        }
    }
}