package at.xirado.jdui.component.message

import at.xirado.jdui.component.ComponentContainer
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.container.Container as JDAContainer

class Container internal constructor(
    var accentColor: Int?,
    var spoiler: Boolean,
) : ComponentContainer<JDAContainer, ContainerChildComponent>() {
    override fun build(uniqueId: Int, children: Collection<ContainerChildComponent>): JDAContainer {
        return JDAContainer.of(children)
            .withSpoiler(spoiler)
            .withAccentColor(accentColor)
            .withUniqueId(uniqueId)
    }

    override val type = typeOf<JDAContainer>()
}

fun container(
    accentColor: Int? = null,
    spoiler: Boolean = false,
    block: Container.() -> Unit,
): Container {
    return Container(accentColor, spoiler).apply(block)
}