package at.xirado.jdui.state

import at.xirado.jdui.view.View
import at.xirado.jdui.view.definition.ViewDefinition
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

fun <T> ViewDefinition.state(default: T): PropertyDelegateProvider<Any?, UserStateProperty<T>> {
    val viewState = this.state
    val userState = this.userState

    if (!viewState.supportUserState)
        throw IllegalStateException("State is not supported in this context")

    return PropertyDelegateProvider { _, property ->
        val delegate = UserStateProperty(userState.size, property as KProperty<T>, default, viewState)
        state.registerUserState(delegate)
        userState += delegate
        delegate
    }
}

fun <T> View.state(default: T): PropertyDelegateProvider<Any?, UserStateProperty<T>> {
    return withDefinition { it.state(default) }
}