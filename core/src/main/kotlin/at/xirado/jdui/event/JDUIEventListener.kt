package at.xirado.jdui.event

import at.xirado.jdui.Context
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.text
import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.definition.function.view
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent

object DefaultEventAdapter : JDUIEventListener()

abstract class JDUIEventListener {
    open suspend fun onUnknownView(event: GenericInteractionCreateEvent, context: Context): ViewDefinition {
        return view {
            compose {
                +container(0xc9211e) {
                    +text("This action timed out.")
                }
            }
        }
    }

    open suspend fun onException(event: GenericInteractionCreateEvent, context: Context, exception: Throwable): ViewDefinition {
        return view {
            compose {
                +container(0xc9211e) {
                    +text("\u009D\u008C Sorry, an unexpected error occurred. Please try again later.")
                }
            }
        }
    }
}