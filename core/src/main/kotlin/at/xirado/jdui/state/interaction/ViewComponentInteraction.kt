package at.xirado.jdui.state.interaction

import at.xirado.jdui.Context
import at.xirado.jdui.JDUIListener
import at.xirado.jdui.component.StatefulActionComponent
import at.xirado.jdui.state.ViewState
import at.xirado.jdui.state.getViewStateByDiscordCustomIds
import at.xirado.jdui.utils.await
import at.xirado.jdui.utils.mergeCustomIds
import at.xirado.jdui.view.ViewDSL
import at.xirado.jdui.view.createFunctionViewState
import at.xirado.jdui.view.definition.function.ViewDefinitionFunction
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import kotlin.reflect.KFunction

@ViewDSL
class ViewComponentInteraction<E: GenericComponentInteractionCreateEvent> private constructor(
    internal val state: ViewState,
    val event: E,
    internal val receivedTimestamp: Long,
) {
    private val mutex = state.mutex
    private var isInitialized = false

    internal var result: ComponentCallbackResult = UpdateMessage

    private suspend fun initialize(): StatefulActionComponent<*, E> {
        val component = getComponentFromEvent()
        isInitialized = true
        return component
    }

    internal suspend fun process() = mutex.withLock {
        if (isInitialized)
            throw IllegalStateException("Can only call processEvent() once!")

        val component = initialize()
        component.processInteraction(this)
        val result = this.result

        when (result) {
            is UpdateMessage -> {
                val message = state.composeMessage()
                event.editMessage(MessageEditData.fromCreateData(message)).queue()
            }
            is SendFollowup -> {
                val ephemeral = result.ephemeral
                event.deferReply(ephemeral).queue()
                state.coroutineScope.launch {
                    val function = result.function
                    val messageData = function()
                    event.hook.sendMessage(messageData).await()
                }
            }
            is DoNothing -> {}
            else -> TODO("Unsupported")
        }
    }

    fun doNothing() {
        result = DoNothing
    }

    private suspend fun getComponentFromEvent() = state.getActionComponentFromEvent(event)

    internal companion object {
        internal suspend fun <E: GenericComponentInteractionCreateEvent> fromEvent(
            jdui: JDUIListener,
            event: E,
            receivedTimestamp: Long,
        ): ViewComponentInteraction<E>? {
            val id = event.message.components.mergeCustomIds()
            val state = getViewStateByDiscordCustomIds(jdui, id) ?: return null

            return ViewComponentInteraction(state, event, receivedTimestamp)
        }
    }
}

fun ViewComponentInteraction<*>.sendFollowup(
    function: KFunction<ViewDefinitionFunction>,
    ephemeral: Boolean = true,
    inheritContext: Boolean = false,
    context: Context? = null,
) {
    val messageFunction: suspend () -> MessageCreateData = {
        val stateContext = when {
            inheritContext && context == null -> Context.copyOf(state.context)
            inheritContext && context != null -> Context.copyOf(state.context).also { it.provideAll(context) }
            else -> context
        }

        val state = createFunctionViewState(event.jda, function, stateContext)
        state.composeMessage()
    }

    result = SendFollowup(messageFunction, ephemeral)
}
