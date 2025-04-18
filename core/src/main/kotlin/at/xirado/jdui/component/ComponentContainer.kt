package at.xirado.jdui.component

import net.dv8tion.jda.api.components.Component as JDAComponent

abstract class ComponentContainer<T, C>: ParentComponent<T, C>() {
    internal val container = mutableListOf<Component<out C>>()

    internal fun appendComponent(component: Component<out C>) {
        container.add(component)
    }

    operator fun <U: Component<out C>> U.unaryPlus() {
        container.add(this)
    }
}

fun <T, C> ComponentContainer<T, C>.count(block: (Component<out Any>) -> Boolean): Int {
    var count = 0

    forEachRecursive {
        if (block(it)) {
            count++
        }
    }

    return count
}

abstract class AccessoryComponentContainer<T, C, A: JDAComponent>: ComponentContainer<T, C>() {
    internal var accessory: Component<out A>? = null

    override fun build(uniqueId: Int, children: Collection<C>): T {
        throw IllegalStateException("Unsupported")
    }

    fun <T: Component<out A>> accessory(block: () -> T) {
        accessory = block()
    }

    abstract fun build(uniqueId: Int, children: Collection<C>, accessory: A?): T
}

internal class ComponentIndex : Iterable<Map.Entry<Int, Component<*>>> {
    private var callbackIndex = 0
    private var nonCallbackIndex = 0
    private var components = mutableMapOf<Int, Component<*>>()

    fun index(component: Component<*>): Int {
        val index = if (component is StatefulActionComponent<*, *>) {
            ++callbackIndex
        } else {
            1000 + nonCallbackIndex++
        }
        components[index] = component
        return index
    }

    fun getComponentByIndex(index: Int): Component<*>? {
        return components[index]
    }

    override fun iterator(): Iterator<Map.Entry<Int, Component<*>>> {
        return components.asIterable().iterator()
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T, C> ComponentContainer<T, C>.buildRecursively(
    id: (StatefulComponent<*>) -> String,
    index: ComponentIndex,
): T {
    val selfIndex = index.index(this)

    val accessoryComponent = (this as? AccessoryComponentContainer<*, *, *>)
        ?.accessory?.let {
            val accessoryIndex = index.index(it)

            when (it) {
                is StatefulComponent<*> -> it.buildComponent(id(it), accessoryIndex)
                is StatelessComponent<*> -> it.buildComponent(accessoryIndex)
                else -> throw IllegalStateException("Unsupported")
            }
        }

    val children = mapDirectChildren(includeAccessory = false) {
        when (it) {
            is ComponentContainer<*, *> -> it.buildRecursively(id, index)
            is StatefulComponent<*> -> it.buildComponent(id(it), index.index(it))
            is StatelessComponent<*> -> it.buildComponent(index.index(it))
            else -> throw IllegalStateException("Unsupported: ${it::class.qualifiedName}")
        }
    } as List<C>

    return if (this is AccessoryComponentContainer<*, *, *>) {
        this as AccessoryComponentContainer<T, C, JDAComponent>
        build(selfIndex, children, accessoryComponent)
    } else {
        build(selfIndex, children)
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T, C, M> ComponentContainer<T, C>.mapDirectChildren(
    includeAccessory: Boolean = true,
    block: (Component<out Any>) -> M
): List<M> {
    val components = mutableListOf<Component<out Any>>()
    
    if (includeAccessory) {
        val accessory = (this as? AccessoryComponentContainer<Any, JDAComponent, JDAComponent>)
            ?.accessory

        if (accessory != null)
            components.add(accessory)
    }

    components.addAll(container as List<Component<out Any>>)
    
    return components.flatMap {
        listOf(block(it))
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T, C> ComponentContainer<T, C>.forEachRecursive(
    block: (Component<out Any>) -> Unit,
) {
    block(this as Component<out Any>)
    mapDirectChildren {
        if (it is ComponentContainer<*, *>)
            it.forEachRecursive(block)
        else
            block(it)
    }
}

internal fun <T, C> ComponentContainer<T, C>.buildIndex(index: ComponentIndex) {
    forEachRecursive {
        index.index(it)
    }
}



