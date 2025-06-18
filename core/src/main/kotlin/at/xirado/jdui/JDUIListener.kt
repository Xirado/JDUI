package at.xirado.jdui

import at.xirado.jdui.config.JDUIConfig
import at.xirado.jdui.handler.ComponentInteractionHandler
import at.xirado.jdui.state.MessageStateRemovalListener
import at.xirado.jdui.state.ViewState
import at.xirado.jdui.utils.newCoroutineScope
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.hooks.EventListener
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger { }

class JDUIListener(internal val config: JDUIConfig) : EventListener {
    internal val snowflakeGen = config.snowflakeGenerator
    internal val context = config.context?.let { Context.copyOf(it) } ?: Context()
    internal val messageCache: Cache<Long, ViewState> = Caffeine.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .removalListener(MessageStateRemovalListener())
        .scheduler(config.scheduler)
        .build()

    internal val coroutineScope = newCoroutineScope<JDUIListener>(
        dispatcher = config.dispatcher,
    )

    internal lateinit var jda: JDA

    private val componentInteractionHandler = ComponentInteractionHandler(this)
//    private val modalInteractionHandler = ModalInteractionHandler(this)

    override fun onEvent(event: GenericEvent) {
        when (event) {
            is StatusChangeEvent -> handleStatusChange(event)
            is ShutdownEvent -> handleShutdown(event)
            is GenericComponentInteractionCreateEvent -> componentInteractionHandler.handleComponentEvent(event)
        }
    }

    private fun handleStatusChange(event: StatusChangeEvent) {
        if (event.newStatus == JDA.Status.LOADING_SUBSYSTEMS) {
            log.debug { "Added JDA session with shard id ${event.jda.shardInfo.shardId} to global state" }
            appendSession(event.jda, this)
        }
    }

    private fun handleShutdown(event: ShutdownEvent) {
        removeSession(event.jda)
    }
}