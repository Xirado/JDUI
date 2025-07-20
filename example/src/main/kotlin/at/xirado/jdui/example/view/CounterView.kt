package at.xirado.jdui.example.view

import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.text
import at.xirado.jdui.component.row
import at.xirado.jdui.state.state
import at.xirado.jdui.view.definition.function.view
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.components.buttons.ButtonStyle

@Serializable
data class LastUpdate(
    val username: String,
    val step: Int,
)

fun counterView() = view {
    var counter: Int by state { 0 }
    var step: Int by state { 1 }
    var lastUpdate: LastUpdate? by state { null }

    compose {
        +text(
            """
                ## Counter: `$counter`
                
                ${lastUpdate?.let { "Last update: +${it.step} by ${it.username}" } ?: "Nobody pressed the button :("}
            """.trimIndent()
        )
        +row {
            +button(ButtonStyle.SECONDARY, "Increment by $step") {
                counter += step
                lastUpdate = LastUpdate(user.name, step)

            }
            +button(ButtonStyle.PRIMARY, "-1", disabled = step == 1) {
                step -= 1
            }
            +button(ButtonStyle.PRIMARY, "+1") {
                step += 1
            }
        }
    }
}