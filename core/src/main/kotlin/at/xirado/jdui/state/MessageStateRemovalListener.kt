package at.xirado.jdui.state

import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.benmanes.caffeine.cache.RemovalListener
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

internal class MessageStateRemovalListener : RemovalListener<Long, ViewState> {
    override fun onRemoval(key: Long?, value: ViewState?, cause: RemovalCause) {
        value ?: return


        log.trace { "Invalidating $value" }
//        coroutineScope.launch {
//            value.invalidate()
//        }
    }
}