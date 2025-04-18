package at.xirado.jdui.view.metadata.source

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal object ViewSourceCache {
    private val sources = mutableMapOf<ViewSourceData, ViewSource>()
    private val lock = ReentrantReadWriteLock()

    fun getViewSource(data: ViewSourceData): ViewSource {
        lock.read {
            sources[data]?.let {
                return it
            }
        }

        val source = createViewSource(data)
        lock.write {
            sources[data] = source
        }

        return source
    }
}

private fun createViewSource(data: ViewSourceData): ViewSource {
    return when (data) {
        is FunctionViewSourceData -> FunctionViewSource(data)
        is ClassViewSourceData -> ClassViewSource(data)
    }
}