package at.xirado.jdui

import net.dv8tion.jda.api.JDA
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val instances: MutableMap<JDA, JDUIListener> = IdentityHashMap()
private val lock = ReentrantReadWriteLock()

internal fun appendSession(jda: JDA, jdui: JDUIListener): Unit = lock.write {
    if (jda !in instances) {
        instances[jda] = jdui
        jdui.jda = jda
    }
}

internal fun removeSession(jda: JDA): Unit = lock.write {
    instances.remove(jda)
}

internal fun getInstance(jda: JDA): JDUIListener = lock.read {
    instances[jda]
} ?: throw IllegalStateException("No JDUIListener registered on this JDA instance")