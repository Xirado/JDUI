package at.xirado.jdui.view

import at.xirado.jdui.state.ViewState
import at.xirado.jdui.view.definition.ViewDefinition

class ViewMessageContainer internal constructor(
    private val state: ViewState,
    definition: ViewDefinition
) {
    private val views = mutableListOf(definition)

}