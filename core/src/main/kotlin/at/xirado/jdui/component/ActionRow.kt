package at.xirado.jdui.component

import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.actionrow.ActionRow as JDAActionRow

class ActionRow : ComponentContainer<JDAActionRow, ActionRowChildComponent>() {
    override fun build(
        uniqueId: Int,
        children: Collection<ActionRowChildComponent>
    ): JDAActionRow {
        return JDAActionRow.of(children).withUniqueId(uniqueId)
    }

    override val type = typeOf<JDAActionRow>()
}

fun row(
    block: ActionRow.() -> Unit
): ActionRow {
    return ActionRow().apply(block)
}