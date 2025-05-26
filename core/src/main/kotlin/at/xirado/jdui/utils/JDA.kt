package at.xirado.jdui.utils

import net.dv8tion.jda.api.components.ActionComponent
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.section.Section

fun Collection<Component>.tryMergeIds(allowedPrefixes: Collection<String>): String? {
    val sb = StringBuilder()

    val result = walkUntil { component ->
        if (component !is ActionComponent)
            return@walkUntil true

        val id = component.customId ?: return@walkUntil true

        if (sb.isEmpty() && allowedPrefixes.none { id.startsWith(it) })
            return@walkUntil false

        sb.append(id)
        true
    }

    return if (!result || sb.isEmpty())
        null
    else
        sb.toString()
}

internal fun Collection<Component>.walkUntil(block: (Component) -> Boolean): Boolean {
    for (component in this) {
        if (!block(component)) return false

        when (component) {
            is ActionRow -> {
                if (!component.components.walkUntil(block))
                    return false
            }
            is Container -> {
                if (!component.components.walkUntil(block))
                    return false
            }
            is Section -> {
                if (!block(component.accessory)) return false
                if (!component.contentComponents.walkUntil(block))
                    return false
            }
        }
    }
    return true
}