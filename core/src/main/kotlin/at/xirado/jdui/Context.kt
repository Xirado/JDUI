package at.xirado.jdui

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Context(
    private var parent: Context? = null,
) {
    private val context = mutableMapOf<String, Any>()
    private val lock = ReentrantReadWriteLock()

    inline fun <reified T: Any> provide(instance: T) {
        provide(T::class, instance)
    }

    fun <T: Any> provide(clazz: KClass<T>, instance: T) {
        val name = clazz.qualifiedName ?: throw IllegalArgumentException("Class does not have a name!")

        lock.write {
            check(name !in context) { "Cannot provide context $name twice!" }
            context[name] = instance
        }
    }

    fun provideAll(other: Context) {
        other.lock.read {
            lock.write {
                context.putAll(other.context)
            }
        }
    }

    inline fun <reified T: Any> get(): T? {
        return get(T::class)
    }

    fun <T: Any> get(clazz: KClass<T>): T? {
        val name = clazz.qualifiedName ?: throw IllegalArgumentException("Class does not have a name!")
        return get(name)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> get(identifier: String): T? {
        return lock.read {
            context[identifier] as? T
        } ?: parent?.get(identifier)
    }

    inline fun <reified T: Any> expect(): T {
        return expect(T::class)
    }

    fun <T: Any> expect(clazz: KClass<T>): T {
        return get(clazz)
            ?: throw NoSuchElementException("Context ${clazz.qualifiedName!!} is not present")
    }

    fun <T> expect(identifier: String): T {
        return get(identifier)
            ?: throw NoSuchElementException("Context $identifier is not present")
    }

    operator fun <T> getValue(thisRef: Any?, property: KProperty<*>): T {
        val returnType = property.returnType
        val clazzName = (returnType.classifier as? KClass<*>)?.qualifiedName
            ?: throw IllegalStateException("Class does not have a name")

        val value: T? = get(clazzName)

        if (value == null) {
            if (returnType.isMarkedNullable)
                return null as T
            throw NoSuchElementException("Context $clazzName is not present")
        }

        return value
    }

    companion object {
        fun copyOf(other: Context) = Context().apply {
            other.lock.read {
                context.putAll(other.context)
            }
        }
    }
}

class ContextBuilder {
    val context = Context()

    inline operator fun <reified T : Any> T.unaryPlus() {
        context.provide(this)
    }
}

fun context(edit: ContextBuilder.() -> Unit): Context = ContextBuilder().apply(edit).context
