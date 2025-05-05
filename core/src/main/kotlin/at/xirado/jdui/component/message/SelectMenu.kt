package at.xirado.jdui.component.message

import at.xirado.jdui.component.ComponentCallback
import at.xirado.jdui.component.StatefulActionComponent
import at.xirado.jdui.state.interaction.ViewComponentInteraction
import net.dv8tion.jda.api.components.selects.SelectOption
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.selects.EntitySelectMenu as JDAEntitySelectMenu
import net.dv8tion.jda.api.components.selects.StringSelectMenu as JDAStringSelectMenu

private typealias StringCallback = suspend StringSelectInteractionEvent.() -> Unit
private typealias EntityCallback = suspend EntitySelectInteractionEvent.() -> Unit

class StringSelectMenu(
    var options: Collection<SelectOption>,
    var range: IntRange,
    var placeholder: String?,
    var disabled: Boolean,
    override val callback: ComponentCallback<StringSelectInteractionEvent>
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

    override suspend fun processInteraction(interaction: ViewComponentInteraction<StringSelectInteractionEvent>) {
        callback(interaction)
    }

    override val type = typeOf<JDAStringSelectMenu>()
    override val eventClazz = StringSelectInteractionEvent::class
}

class EntitySelectMenu(
    var targets: Collection<JDAEntitySelectMenu.SelectTarget>,
    var channelTypes: Collection<ChannelType>,
    var range: IntRange,
    var placeholder: String?,
    var disabled: Boolean,
    override val callback: ComponentCallback<EntitySelectInteractionEvent>,
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

    override suspend fun processInteraction(interaction: ViewComponentInteraction<EntitySelectInteractionEvent>) {
        callback(interaction)
    }

    override val type = typeOf<JDAEntitySelectMenu>()
    override val eventClazz = EntitySelectInteractionEvent::class
}

fun stringSelect(
    options: Collection<SelectOption>,
    range: IntRange = 1..1,
    placeholder: String? = null,
    disabled: Boolean = false,
    callback: ComponentCallback<StringSelectInteractionEvent>
): StringSelectMenu {
    return StringSelectMenu(options, range, placeholder, disabled, callback)
}

fun entitySelect(
    targets: Collection<JDAEntitySelectMenu.SelectTarget>,
    channelTypes: Collection<ChannelType> = ChannelType.guildTypes(),
    range: IntRange = 1..1,
    placeholder: String? = null,
    disabled: Boolean = false,
    callback: ComponentCallback<EntitySelectInteractionEvent>
) : EntitySelectMenu {
    return EntitySelectMenu(targets, channelTypes, range, placeholder, disabled, callback)
}