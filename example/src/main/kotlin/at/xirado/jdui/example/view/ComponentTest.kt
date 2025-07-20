package at.xirado.jdui.example.view

import at.xirado.jdui.component.message.*
import at.xirado.jdui.component.row
import at.xirado.jdui.state.state
import at.xirado.jdui.view.definition.function.view
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.FileUpload

private val emojis = mapOf(
    "Duck" to "\uD83E\uDD86",
    "Cat" to "\uD83D\uDC31",
    "Skull" to "\uD83D\uDC80",
    "Eggplant" to "\uD83C\uDF46",
    "Peach" to "\uD83C\uDF51",
)

fun componentTest() = view {
    var description: String? by state(null)
    var emojiString: String by state("")

    val file = {
        val bytes = "Hello World".byteInputStream()
        FileUpload.fromData(bytes, "test.txt")
    }

    compose {
        description?.let { +text(it) }
        +container(0x42b9f5) {
            val accessoryButton = button(ButtonStyle.SECONDARY, "Accessory button") {
                description = "Accessory button was used"
            }

            +section(accessoryButton) {
                +text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce ultricies vestibulum nisi, at ullamcorper sapien euismod vitae.")
                +text("Proin lacinia gravida pretium. Etiam fringilla mauris sed tincidunt tempor. Proin eu lacus.")
            }
        }
        +container(0x42b9f5) {
            +section(accessory = thumbnail("https://cdn.xirado.dev/tvpattern.jpg")) {
                +text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce ultricies vestibulum nisi, at ullamcorper sapien euismod vitae.")
                +text("Proin lacinia gravida pretium. Etiam fringilla mauris sed tincidunt tempor. Proin eu lacus.")
            }
        }

        +container(0x42b9f5) {
            +mediaGallery {
                +MediaGalleryItem.fromUrl("https://cdn.xirado.dev/tvpattern.jpg")
                +MediaGalleryItem.fromUrl("https://cdn.xirado.dev/tvpattern.jpg")
            }
            +fileDisplay(file())
        }

        +container(0x42b9f5) {
            if (emojiString.isNotBlank())
                +text(emojiString)
            +row {
                emojis.values.forEach {
                    +button(ButtonStyle.SECONDARY, emoji = Emoji.fromUnicode(it)) {
                        emojiString += it
                    }
                }
            }
            +row {
                val options = emojis.map { SelectOption.of(it.key, it.value).withEmoji(Emoji.fromUnicode(it.value)) }
                +stringSelect(options, placeholder = "Select an emoji") {
                    selectedOptions.forEach { emojiString += it.value }
                }
            }
            +row {
                val selectTargets = listOf(EntitySelectMenu.SelectTarget.CHANNEL)
                +entitySelect(selectTargets, placeholder = "Select a channel") {
                    println("Test")
                }
            }
        }
    }
}