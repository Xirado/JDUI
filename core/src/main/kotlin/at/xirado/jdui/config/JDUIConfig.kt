package at.xirado.jdui.config

import at.xirado.jdui.Context
import at.xirado.jdui.event.DefaultEventAdapter
import at.xirado.jdui.event.JDUIEventListener
import com.github.benmanes.caffeine.cache.Scheduler
import de.mkammerer.snowflakeid.SnowflakeIdGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KClass

/**
 * Configuration for JDUI
 */
class JDUIConfigBuilder internal constructor(block: JDUIConfigBuilder.() -> Unit = {}) {
    /**
     * Generator ID for creation of snowflakes.
     * This has to be unique across all [JDUIListener][at.xirado.jdui.JDUIListener] instances
     *
     * Default: 0
     */
    var generatorId: Int = 0

    /**
     * [SnowflakeIdGenerator] used for creating unique ids.
     *
     * If none is provided, a default one, with [generatorId] as the generator id, is used.
     */
    var snowflakeIdGenerator: SnowflakeIdGenerator? = null

    /**
     * [EventAdapter][JDUIEventListener] used to handle certain events emitted by JDUI. Here you can configure for example what happens when an
     * exception is thrown anywhere in the MessageView flow.
     */
    var eventAdapter: JDUIEventListener = DefaultEventAdapter

    /**
     * [CoroutineDispatcher] used for handling the MessageView flow.
     * Default: [Dispatchers.Default]
     */
    var coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * [PersistenceConfig] used for storing and retrieving [persistent message views][at.xirado.jdui.persistence.PersistentMessageView]
     */
    var persistenceConfig: PersistenceConfig? = null

    /**
     * [Scheduler] used for bookkeeping the MessageView cache
     * Default: [Scheduler.systemScheduler]
     */
    var scheduler: Scheduler = Scheduler.systemScheduler()

    /**
     * Secret used for encrypting view state in component ids.
     * This makes it impossible for a third party to decipher a views' state.
     */
    var secret: Secret? = null

    private var context: Context? = null

    init {
        apply(block)
        require(generatorId >= 0) { "generatorId must be greater than or equal 0" }
        require(generatorId <= 1024) { "generatorId must be smaller than or equal 1024" }
        require(secret != null) { "secret must be set!" }
    }

    inline fun <reified T: Any> provideContext(instance: T) {
        provideContext(T::class, instance)
    }

    fun <T: Any> provideContext(clazz: KClass<T>, instance: T) {
        val context = context ?: run {
            Context().also { context = it }
        }

        context.provide(clazz, instance)
    }

    internal fun build(): JDUIConfig {
        require(secret != null) { "Secret must be set" }

        return JDUIConfig(
            snowflakeIdGenerator ?: SnowflakeIdGenerator.createDefault(generatorId),
            coroutineDispatcher,
            eventAdapter,
            scheduler,
            secret!!,
            context,
            persistenceConfig,
        )
    }
}

class JDUIConfig(
    val snowflakeGenerator: SnowflakeIdGenerator,
    val dispatcher: CoroutineDispatcher,
    val eventAdapter: JDUIEventListener,
    val scheduler: Scheduler,
    val secret: Secret,
    val context: Context?,
    val persistenceConfig: PersistenceConfig?,
)

fun jdui(config: JDUIConfigBuilder.() -> Unit): JDUIConfig {
    val builder = JDUIConfigBuilder(config)
    return builder.build()
}