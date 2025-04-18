package at.xirado.jdui.view

import at.xirado.jdui.view.definition.ViewDefinition
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KFunction

class ViewFunctionCache {
    private val functions: MutableMap<String, KFunction<ViewDefinition>> = mutableMapOf()
    private val rwLock = ReentrantReadWriteLock()

    fun get(qualifier: String): KFunction<ViewDefinition>? = rwLock.read {
        functions[qualifier]
    }

    fun put(qualifier: String, function: KFunction<ViewDefinition>) {
        return rwLock.write {
            functions[qualifier] = function
        }
    }
}