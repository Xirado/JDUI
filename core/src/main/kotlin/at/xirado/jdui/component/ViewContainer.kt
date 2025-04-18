package at.xirado.jdui.component

import at.xirado.jdui.view.definition.ViewDefinition
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import kotlin.reflect.typeOf

class ViewContainer : ComponentContainer<MessageCreateData, MessageTopLevelComponent>() {
    override fun build(uniqueId: Int, children: Collection<MessageTopLevelComponent>): MessageCreateData {
        return MessageCreateBuilder()
            .useComponentsV2()
            .setComponents(children)
            .build()
    }

    suspend fun ViewDefinition.unaryPlus() {
        if (!isInitialized) {
            initialize(state)
        }

        val view = createView()
        container.addAll(view.container)
    }

    override val type = typeOf<MessageCreateData>()
}