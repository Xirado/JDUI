package at.xirado.jdui.view.definition.function

import at.xirado.jdui.component.ViewContainer
import at.xirado.jdui.view.ViewDSL
import at.xirado.jdui.view.definition.ViewDefinition

internal typealias ViewFunction = suspend ViewDefinitionFunction.() -> Unit
internal typealias ComposeFunction = suspend ViewContainer.() -> Unit

@ViewDSL
class ViewDefinitionFunction(
    private val viewFunction: ViewFunction
) : ViewDefinition() {
    private lateinit var composeFunction: ComposeFunction

    override suspend fun initialize() {
        viewFunction(this)
        if (!::composeFunction.isInitialized)
            throw IllegalStateException("compose() function must be used!")

    }

    override suspend fun createView(): ViewContainer {
        val container = ViewContainer()
        composeFunction(container)
        return container
    }

    fun compose(block: ComposeFunction) {
        composeFunction = block
    }
}

fun view(block: ViewFunction): ViewDefinitionFunction {
    val definition = ViewDefinitionFunction(block)
    return definition
}