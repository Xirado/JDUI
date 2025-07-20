package at.xirado.jdui.component.message

import at.xirado.jdui.component.StatefulActionComponent
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.selections.EntitySelectMenu as JDAEntitySelectMenu
import net.dv8tion.jda.api.components.selections.StringSelectMenu as JDAStringSelectMenu

private typealias StringCallback = suspend StringSelectInteractionEvent.() -> Unit
private typealias EntityCallback = suspend EntitySelectInteractionEvent.() -> Unit

class StringSelectMenu(
    var options: Collection<SelectOption>,
    var range: IntRange,
    var placeholder: String?,
    var disabled: Boolean,
    override val callback: StringCallback
) : StatefulActionComponent<JDAStringSelectMenu, StringSelectInteractionEvent>() {
    override fun buildComponent(id: String, uniqueId: Int): JDAStringSelectMenu {
        return JDAStringSelectMenu.create(id)
            .addOptions(options)
            .setRequiredRange(range.first, range.last)
            .setPlaceholder(placeholder)
            .setDisabled(disabled)
            .setUniqueId(uniqueId)
            .build()
    }

    override suspend fun processInteraction(event: GenericComponentInteractionCreateEvent) {
        callback(event as StringSelectInteractionEvent)
    }

    override val type = typeOf<JDAStringSelectMenu>()
    override val callbackClazz = StringSelectInteractionEvent::class
}

class EntitySelectMenu(
    var targets: Collection<JDAEntitySelectMenu.SelectTarget>,
    var channelTypes: Collection<ChannelType>,
    var range: IntRange,
    var placeholder: String?,
    var disabled: Boolean,
    override val callback: EntityCallback,
) : StatefulActionComponent<JDAEntitySelectMenu, EntitySelectInteractionEvent>() {
    override fun buildComponent(id: String, uniqueId: Int): JDAEntitySelectMenu {
        return JDAEntitySelectMenu.create(id, targets)
            .setRequiredRange(range.first, range.last)
            .setChannelTypes(channelTypes)
            .setPlaceholder(placeholder)
            .setDisabled(disabled)
            .setUniqueId(uniqueId)
            .build()
    }

    override suspend fun processInteraction(event: GenericComponentInteractionCreateEvent) {
        callback(event as EntitySelectInteractionEvent)
    }

    override val type = typeOf<JDAEntitySelectMenu>()
    override val callbackClazz = EntitySelectInteractionEvent::class
}

fun stringSelect(
    options: Collection<SelectOption>,
    range: IntRange = 1..1,
    placeholder: String? = null,
    disabled: Boolean = false,
    callback: StringCallback
): StringSelectMenu {
    return StringSelectMenu(options, range, placeholder, disabled, callback)
}

fun entitySelect(
    targets: Collection<JDAEntitySelectMenu.SelectTarget>,
    channelTypes: Collection<ChannelType> = ChannelType.guildTypes(),
    range: IntRange = 1..1,
    placeholder: String? = null,
    disabled: Boolean = false,
    callback: EntityCallback
) : EntitySelectMenu {
    return EntitySelectMenu(targets, channelTypes, range, placeholder, disabled, callback)
}