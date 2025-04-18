package at.xirado.jdui.example.view

import at.xirado.jdui.component.message.Container
import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.text
import at.xirado.jdui.component.row
import at.xirado.jdui.state.state
import at.xirado.jdui.view.View
import at.xirado.jdui.view.compose
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.TimeFormat

class MultipleMenusView : View() {
    private var isConfirmationView by state(false)
    private var isLoading by state(false)
    private var data: String? by state(null)

    override suspend fun createView() = compose {
        +container(0x4287f5) {
            if (data == null)
                +text("Data is not loaded yet :(")
            else
                +text("Data is: $data")

            if (isConfirmationView) {
                confirmationView()
                return@container
            }

            if (isLoading) {
                val loadingEmoji = Emoji.fromCustom("pet_the_jda", 1156377931573116978, true)
                +text("${loadingEmoji.asMention} Loading data... Please wait.")
            }

            if (!isConfirmationView) {
                +row {
                    +button(ButtonStyle.SECONDARY, "Load data", disabled = isLoading) {
                        isConfirmationView = true
                    }
                }
            }
        }
    }

    private fun Container.confirmationView() {
        +text("Are you sure you want to do this?")
        +row {
            +button(ButtonStyle.PRIMARY, "Continue") {
                isConfirmationView = false
                isLoading = true
                startLoadingData()
            }
            +button(ButtonStyle.SECONDARY, "Cancel") {
                isConfirmationView = false
            }
        }
    }

    private fun startLoadingData() {
        coroutineScope.launch {
            delay(5000)
            locked {
                data = "Hello, World! ${TimeFormat.RELATIVE.now()}"
                isLoading = false
                triggerMessageUpdate()
            }
        }
    }
}