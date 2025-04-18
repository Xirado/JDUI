package at.xirado.jdui.component.message

import at.xirado.jdui.component.StatelessComponent
import net.dv8tion.jda.api.components.separator.Separator.Spacing
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.separator.Separator as JDASeparator

class Separator(
    var isDivider: Boolean,
    var spacing: Spacing,
): StatelessComponent<JDASeparator>() {
    override fun buildComponent(uniqueId: Int): JDASeparator {
        return JDASeparator.create(isDivider, spacing)
            .withUniqueId(uniqueId)
    }

    override val type = typeOf<JDASeparator>()
}

fun separator(
    isDivider: Boolean,
    spacing: Spacing
): Separator {
   return Separator(isDivider, spacing)
}