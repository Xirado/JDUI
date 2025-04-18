package at.xirado.jdui.state

import kotlinx.serialization.serializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class UserStateProperty<T> internal constructor(
    internal val index: Int,
    internal val property: KProperty<T>,
    internal val default: T,
    internal val state: ViewState,
): ReadWriteProperty<Any?, T> {
    internal val serializer = serializer(property.returnType)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return state.getUserState(this)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        state.setUserState(this, value)
    }
}