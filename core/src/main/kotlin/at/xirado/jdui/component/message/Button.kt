package at.xirado.jdui.component.message

import at.xirado.jdui.component.StatefulActionComponent
import at.xirado.jdui.component.StatelessComponent
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KClass
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.button.Button as JDAButton

class ActionButton(
    var style: ButtonStyle,
    var label: String?,
    var emoji: Emoji?,
    var disabled: Boolean,
    override val callback: suspend ButtonInteractionEvent.() -> Unit,
) : StatefulActionComponent<JDAButton, ButtonInteractionEvent>() {
    override fun buildComponent(id: String, uniqueId: Int): JDAButton {
        return JDAButton.of(style, id, label, emoji)
            .withDisabled(disabled)
            .withUniqueId(uniqueId)
    }

    override suspend fun processInteraction(event: GenericComponentInteractionCreateEvent) {
        callback(event as ButtonInteractionEvent)
    }

    override val type = typeOf<JDAButton>()
    override val callbackClazz = ButtonInteractionEvent::class
}

class LinkButton(
    var url: String,
    var emoji: Emoji?,
    var label: String?,
    var disabled: Boolean,
) : StatelessComponent<JDAButton>() {
    override fun buildComponent(uniqueId: Int): JDAButton {
        return JDAButton.of(ButtonStyle.LINK, url, label, emoji)
            .withDisabled(disabled)
            .withUniqueId(uniqueId)
    }

    override val type = typeOf<JDAButton>()
}

fun button(
    style: ButtonStyle,
    label: String? = null,
    emoji: Emoji? = null,
    disabled: Boolean = false,
    callback: suspend ButtonInteractionEvent.() -> Unit,
): ActionButton {
    require(style != ButtonStyle.LINK) { "Cannot use ButtonStyle.LINK here. Use link() instead" }
    return ActionButton(style, label, emoji, disabled, callback)
}

fun link(
    url: String,
    label: String? = null,
    emoji: Emoji? = null,
    disabled: Boolean = false,
): LinkButton {
    return LinkButton(url, emoji, label, disabled)
}
