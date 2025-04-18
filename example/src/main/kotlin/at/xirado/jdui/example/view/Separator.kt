package at.xirado.jdui.example.view

import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.separator
import at.xirado.jdui.component.message.text
import at.xirado.jdui.view.definition.function.view
import net.dv8tion.jda.api.components.separator.Separator.Spacing

fun separatorTestView() = view {
    compose {
        +container {
            +text("Divider with small spacing")
            +separator(true, Spacing.SMALL)
            +text("Divider with small spacing")
        }
        +container {
            +text("Divider with large spacing")
            +separator(true, Spacing.LARGE)
            +text("Divider with large spacing")
        }
        +container {
            +text("No divider with small spacing")
            +separator(false, Spacing.SMALL)
            +text("No divider with small spacing")
        }
        +container {
            +text("No divider with large spacing")
            +separator(false, Spacing.LARGE)
            +text("No divider with large spacing")
        }
    }
}