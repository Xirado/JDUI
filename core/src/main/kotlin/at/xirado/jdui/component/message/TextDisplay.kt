package at.xirado.jdui.component.message

import at.xirado.jdui.component.StatelessComponent
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.textdisplay.TextDisplay as JDATextDisplay

class TextDisplay(
    var content: String
) : StatelessComponent<JDATextDisplay>() {
    override fun buildComponent(uniqueId: Int): JDATextDisplay {
        return JDATextDisplay.of(content).withUniqueId(uniqueId)
    }

    override val type = typeOf<JDATextDisplay>()
}

fun text(content: String): TextDisplay {
    return TextDisplay(content)
}