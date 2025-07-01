package at.xirado.jdui.view.middleware

import at.xirado.jdui.view.View
import at.xirado.jdui.view.definition.ViewDefinition
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

interface ViewMiddleware {
    suspend fun processEvent(event: GenericComponentInteractionCreateEvent): Boolean
}

fun ViewDefinition.middleware(middleware: ViewMiddleware) {
    state.middleware += middleware
}

fun View.middleware(middleware: ViewMiddleware) {
    withDefinition { it.state.middleware += middleware }
}