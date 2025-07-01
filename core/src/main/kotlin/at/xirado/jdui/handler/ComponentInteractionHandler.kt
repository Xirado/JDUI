package at.xirado.jdui.handler

import at.xirado.jdui.JDUIListener
import at.xirado.jdui.state.interaction.ViewComponentInteraction
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

private val componentsV2Flag = Message.MessageFlag.IS_COMPONENTS_V2.value.toLong()

internal class ComponentInteractionHandler(private val jdui: JDUIListener) {
    private val coroutineScope = jdui.coroutineScope

    fun handleComponentEvent(event: GenericComponentInteractionCreateEvent) {
        if ((event.message.flagsRaw and componentsV2Flag) == 0L)
            return

        val now = System.currentTimeMillis()

        coroutineScope.launch {
            val interaction = ViewComponentInteraction.fromEvent(jdui, event, now)
            interaction?.process()
        }
    }
}