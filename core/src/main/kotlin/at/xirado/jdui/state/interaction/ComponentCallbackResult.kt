package at.xirado.jdui.state.interaction

import net.dv8tion.jda.api.utils.messages.MessageCreateData

sealed interface ComponentCallbackResult

// Update the message (default behaviour)
object UpdateMessage : ComponentCallbackResult
// Do not respond to the interaction
object DoNothing : ComponentCallbackResult

class SendFollowup(val function: suspend () -> MessageCreateData, val ephemeral: Boolean) : ComponentCallbackResult

