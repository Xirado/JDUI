package at.xirado.jdui.view.definition

import at.xirado.jdui.Context
import at.xirado.jdui.component.ViewContainer
import at.xirado.jdui.state.UserStateProperty
import at.xirado.jdui.state.ViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.withLock

abstract class ViewDefinition internal constructor() {
    internal lateinit var state: ViewState
    internal var userState: MutableList<UserStateProperty<*>> = mutableListOf()
    internal var isInitialized = false

    val coroutineScope: CoroutineScope
        get() = state.coroutineScope

    val context: Context
        get() = state.context

    internal suspend fun initialize(state: ViewState) {
        if (isInitialized)
            throw IllegalStateException("Cannot initialize a ViewDefinition twice!")
        this.state = state
        initialize()
        isInitialized = true
    }

    suspend fun <T> locked(block: suspend () -> T): T {
        return state.mutex.withLock { block() }
    }

    fun triggerMessageUpdate() {
        state.triggerMessageUpdate()
    }

    abstract suspend fun initialize()

    abstract suspend fun createView(): ViewContainer
}
