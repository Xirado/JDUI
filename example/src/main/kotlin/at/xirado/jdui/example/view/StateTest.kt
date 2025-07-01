package at.xirado.jdui.example.view

import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.text
import at.xirado.jdui.component.row
import at.xirado.jdui.state.state
import at.xirado.jdui.view.definition.function.view
import net.dv8tion.jda.api.components.buttons.ButtonStyle.*

fun stateTest() = view {
    var someInt by state { 0 }
    var someString by state { "" }
    var someNullableInt: Int? by state { null }
    var someNullableString: String? by state { null }

    compose {
        +container(0x00ff00) {
            +text("""
                -# someInt: **$someInt**
                -# someString: **\"$someString\"**
                -# someNullableInt: **$someNullableInt**
                -# someNullableString: **${someNullableString?.let { "\"$it\"" }}**
                
            """.trimIndent())

            +row {
                +button(SECONDARY, "Toggle someInt") {
                    someInt = if (someInt == 0) 1 else 0
                }
                +button(SECONDARY, "Toggle someString") {
                    someString = if (someString.isEmpty()) "A" else ""
                }
                +button(SECONDARY, "Toggle someNullableInt") {
                    someNullableInt = when (someNullableInt) {
                        null -> 0
                        0 -> 1
                        else -> null
                    }
                }
                +button(SECONDARY, "Toggle someNullableString") {
                    someNullableString = when (someNullableString) {
                        null -> ""
                        "" -> "A"
                        else -> null
                    }
                }
                +button(SUCCESS, "Do nothing") {}
            }
        }
    }
}