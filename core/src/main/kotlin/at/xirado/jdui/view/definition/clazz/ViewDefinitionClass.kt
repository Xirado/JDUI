package at.xirado.jdui.view.definition.clazz

import at.xirado.jdui.component.ViewContainer
import at.xirado.jdui.view.View
import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.instantiateViewClass
import kotlin.reflect.KClass

internal class ViewDefinitionClass(
    private val clazz: KClass<out View>,
    view: View?,
) : ViewDefinition() {
    internal lateinit var view: View

    init {
        view?.let { this.view = it }
    }

    override suspend fun initialize() {
        if (!::view.isInitialized) {
            view = clazz.instantiateViewClass(this)
            view.initialize()
        } else {
            view.tryInitialize(this)
        }
    }

    override suspend fun createView(): ViewContainer {
        return view.createView()
    }
}