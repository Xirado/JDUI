package at.xirado.jdui.component

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KClass
import kotlin.reflect.KType
import net.dv8tion.jda.api.components.Component as JDAComponent

abstract class Component<T> {
    internal abstract val type: KType
}

abstract class StatefulComponent<T: JDAComponent> : Component<T>() {
    internal abstract fun buildComponent(id: String, uniqueId: Int): T
}

abstract class StatelessComponent<T: JDAComponent> : Component<T>() {
    internal abstract fun buildComponent(uniqueId: Int): T
}

abstract class StatefulActionComponent<T: JDAComponent, E: Any> : StatefulComponent<T>() {
    internal abstract val callback: suspend E.() -> Unit
    internal abstract val callbackClazz: KClass<E>

    internal abstract suspend fun processInteraction(event: GenericComponentInteractionCreateEvent)
}

abstract class ParentComponent<T, C> : Component<T>() {
    abstract fun build(uniqueId: Int, children: Collection<C>): T
}