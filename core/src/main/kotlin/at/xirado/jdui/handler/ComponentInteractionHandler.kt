package at.xirado.jdui.handler

import at.xirado.jdui.JDUIListener
import at.xirado.jdui.state.interaction.ViewComponentInteraction
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

private val log = KotlinLogging.logger { }

internal class ComponentInteractionHandler(private val jdui: JDUIListener) {
    private val config = jdui.config

    suspend fun handleComponentEvent(event: GenericComponentInteractionCreateEvent) {
        val now = System.currentTimeMillis()

        val interaction = when (event) {
            is ButtonInteractionEvent -> ViewComponentInteraction.fromEvent(jdui, event, now)
            is StringSelectInteractionEvent -> ViewComponentInteraction.fromEvent(jdui, event, now)
            is EntitySelectInteractionEvent -> ViewComponentInteraction.fromEvent(jdui, event, now)
            else -> TODO("Unsupported")
        } ?: return

        interaction.process()
    }
}