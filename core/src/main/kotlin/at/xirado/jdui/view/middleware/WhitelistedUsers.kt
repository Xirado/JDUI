package at.xirado.jdui.view.middleware

import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

private val NOT_WHITELISTED_MESSAGE = MessageCreateBuilder()
    .useComponentsV2()
    .addComponents(
        Container.of(
            TextDisplay.of("You cannot interact with this message!")
        ).withAccentColor(0xFF0000)
    )
    .build()

class WhitelistedUsers(val userIds: Collection<Long>) : ViewMiddleware {
    override suspend fun processEvent(event: GenericComponentInteractionCreateEvent): Boolean {
        val isWhitelisted = event.user.idLong in userIds

        if (!isWhitelisted)
            event.reply(NOT_WHITELISTED_MESSAGE).setEphemeral(true).queue()

        return isWhitelisted
    }
}