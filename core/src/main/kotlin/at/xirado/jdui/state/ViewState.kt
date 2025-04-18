package at.xirado.jdui.state

import at.xirado.jdui.Context
import at.xirado.jdui.JDUIListener
import at.xirado.jdui.component.*
import at.xirado.jdui.config.ViewData
import at.xirado.jdui.crypto.encrypt
import at.xirado.jdui.utils.encode
import at.xirado.jdui.utils.splitIntoParts
import at.xirado.jdui.view.definition.ViewDefinition
import at.xirado.jdui.view.metadata.MessageContext
import at.xirado.jdui.view.metadata.ViewIdentifier
import at.xirado.jdui.view.metadata.ViewMetadata
import at.xirado.jdui.view.metadata.source.ClassViewSourceData
import at.xirado.jdui.view.metadata.source.FunctionViewSourceData
import at.xirado.jdui.view.metadata.source.ViewSourceCache
import at.xirado.jdui.view.metadata.source.ViewSourceData
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.lang.ref.WeakReference

private val log = KotlinLogging.logger { }

class ViewState internal constructor(
    internal val listener: JDUIListener,
    internal val metadata: ViewMetadata,
    internal val viewDefinition: ViewDefinition,
    internal val supportUserState: Boolean,
) {
    internal var coroutineScope = createCoroutineScope(metadata.viewIdentifier)
    internal var componentIndex: ComponentIndex? = null
    internal var mutex = Mutex()
    internal val context = Context(listener.context)
    internal val messageContext = MessageContext(WeakReference(listener.jda))

    private var userState = metadata.viewIdentifier.sourceData?.let {
        if (supportUserState) UserStateCollection(metadata.userState) else null
    }

    internal suspend fun initialize() {
        viewDefinition.initialize(this)
    }

    internal suspend fun createComposeState(): ViewContainer {
        val container = viewDefinition.createView()
        return container
    }

    internal fun triggerMessageUpdate() {
        coroutineScope.launch {
            val message = composeMessage()
            messageContext.editMessage(MessageEditData.fromCreateData(message))
        }
    }

    internal suspend fun composeMessage(): MessageCreateData {
        val container = createComposeState()

        val (message, state) = buildComponents(container)

        if (metadata.viewIdentifier.sourceData != null) {
            coroutineScope.launch {
                saveStateToDatabase(state)
            }
        }

        return message
    }

    private fun buildComponents(
        container: ViewContainer,
    ): Pair<MessageCreateData, ByteArray> {
        val statefulComponentCount = container.count {
            it is StatefulComponent<*>
        }

        val state = createViewMetadata()

        val message = if (statefulComponentCount == 0) {
            val index = ComponentIndex()
            val message = container.buildRecursively({ "" }, index)

            componentIndex = index
            message
        } else {
            val encodedState = encode(state)
            var componentIds = "j1:$encodedState"

            val length = componentIds.length
            val capacity = statefulComponentCount * 100

            if (length > capacity) {
                componentIds = "j2:${metadata.viewIdentifier.id}"
            }

            log.trace { "Length: ${componentIds.length} Capacity: $capacity, Id: $componentIds" }
            val idSplit = componentIds.splitIntoParts(statefulComponentCount, 100)

            var statefulComponentIndex = 0
            val idFunction: (StatefulComponent<*>) -> String = {
                idSplit[statefulComponentIndex++]
            }

            val index = ComponentIndex()
            val message = container.buildRecursively(idFunction, index)
            componentIndex = index
            message
        }

        return message to state
    }

    private suspend fun saveStateToDatabase(data: ByteArray) {
        val persistenceConfig = listener.config.persistenceConfig
            ?: throw IllegalStateException("No PersistenceConfig!")

        persistenceConfig.save(ViewData(metadata.viewIdentifier.id, data))
    }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
    private fun createViewMetadata(): ByteArray {
        val userStateSerialized = userState?.serializeAndPackUserData() ?: ByteArray(0)
        metadata.userState = userStateSerialized
        val metadataSerialized = ProtoBuf.encodeToByteArray(metadata)

        val key = listener.config.secret.derivedKey
        return encrypt(metadataSerialized, key)
    }

    private suspend fun getComponentIndex(): ComponentIndex {
        componentIndex?.let { return it }

        val index = ComponentIndex()
        val container = createComposeState()
        container.buildIndex(index)

        componentIndex = index
        return index
    }

    internal fun <T> getUserState(property: UserStateProperty<T>): T {
        val userState = this.userState
            ?: throw IllegalStateException("View cannot have a state!")
        return userState.getUserState(property)
    }

    internal fun <T> setUserState(property: UserStateProperty<T>, value: T) {
        val userState = this.userState
            ?: throw IllegalStateException("View cannot have a state!")

        return userState.setUserState(property, value)
    }

    internal suspend fun handleComponentInteraction(event: GenericComponentInteractionCreateEvent) {
        val uniqueId = event.uniqueId
        val componentIndex = getComponentIndex()

        val component = componentIndex.getComponentByIndex(uniqueId)
            ?: throw IllegalStateException("Component with unique id ($uniqueId) not found. Broken view state?")

        if (component !is StatefulActionComponent<*, *>)
            throw IllegalStateException("Component is not an action component. Broken view state?")

        val thisEventClazz = event::class
        val componentEventClazz = component.callbackClazz

        if (thisEventClazz != componentEventClazz)
            throw IllegalStateException("Expected event of type $componentEventClazz but got $thisEventClazz. Broken view state?")

        component.processInteraction(event)
    }

    private fun createCoroutineScope(identifier: ViewIdentifier): CoroutineScope {
        val className = identifier.sourceData.let {
            when (it) {
                is ClassViewSourceData -> it.clazzName
                is FunctionViewSourceData -> it.className
                else -> ViewState::class.qualifiedName!!
            }
        }

        val job = Job()
        val dispatcher = listener.config.dispatcher
        val logger = KotlinLogging.logger(className)

        val exceptionHandler = CoroutineExceptionHandler { context, t ->
            logger.error(t) { "Unhandled exception in view ${metadata.viewIdentifier}" }
            job.cancel(CancellationException("An unhandled exception occurred", t))
        }

        return CoroutineScope(job + dispatcher + exceptionHandler)
    }
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         

internal suspend fun createViewState(
    jdui: JDUIListener,
    metadata: ViewMetadata,
    useCache: Boolean = true,
    context: Context? = null,
): ViewState {
    val identifier = metadata.viewIdentifier
    val sourceData = identifier.sourceData
        ?: throw IllegalStateException("Metadata does not contain SourceData!")

    val source = ViewSourceCache.getViewSource(sourceData)
    val definition = source.create()

    val state = ViewState(jdui, metadata, definition, true)
    context?.let { state.context.provideAll(it) }

    state.initialize()

    if (useCache)
        jdui.messageCache.put(identifier.id, state)

    return state
}

internal suspend fun createViewState(
    jdui: JDUIListener,
    sourceData: ViewSourceData,
    useCache: Boolean = true,
    context: Context? = null,
    ): ViewState {
    val id = jdui.snowflakeGen.next()
    val identifier = ViewIdentifier(id, sourceData)

    val metadata = ViewMetadata(identifier, ByteArray(0))
    return createViewState(jdui, metadata, useCache, context)
}

internal suspend fun createViewState(
    jdui: JDUIListener,
    definition: ViewDefinition,
    useCache: Boolean = true,
    context: Context? = null,
): ViewState {
    val id = jdui.snowflakeGen.next()
    val identifier = ViewIdentifier(id, null)
    val metadata = ViewMetadata(identifier, ByteArray(0))

    val state = ViewState(jdui, metadata, definition, false)
    context?.let { state.context.provideAll(it) }

    state.initialize()

    if (useCache)
        jdui.messageCache.put(id, state)

    return state
}