package at.xirado.jdui.utils

import net.dv8tion.jda.api.components.ActionComponent
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.section.Section

fun Collection<Component>.mergeCustomIds(): String {
    val sb = StringBuilder()

    forEach { component ->
        when (component) {
            is ActionRow -> sb.append(component.components.mergeCustomIds())
            is Container -> sb.append(component.components.mergeCustomIds())
            is ActionComponent -> component.customId?.let { sb.append(it) }
            is Section -> {
                if (component.accessory is Button) {
                    sb.append(component.accessory.asButton().customId)
                }
                component.contentComponents.mergeCustomIds()
            }
        }
    }

    return sb.toString()
}