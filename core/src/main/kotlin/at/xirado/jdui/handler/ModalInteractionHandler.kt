package at.xirado.jdui.handler

//import at.xirado.jdui.JDUIListener
//import io.github.oshai.kotlinlogging.KotlinLogging
//import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
//
//private val modalRegex = """(\w).(\d+).(\d+).(\d+)""".toRegex()
//private val log = KotlinLogging.logger { }
//
//internal class ModalInteractionHandler(
//    val listener: JDUIListener
//) {
//    private val eventAdapter = listener.config.eventAdapter
//
//    suspend fun onModalEvent(event: ModalInteractionEvent) {
//        val (type, stateId, modalId, userCounter) = modalRegex.matchEntire(event.modalId)?.destructured
//            ?: return
//
//        log.debug { "Got modal event: ${event.modalId}" }
//        val stateIdLong = stateId.toLong()
//        val modalIdInt = modalId.toInt()
//
//        try {
//            when (type) {
//                "t" -> handleTempView(event, stateIdLong, modalIdInt, userCounter.toInt())
//                "p" -> handlePersistentView(event, stateIdLong, modalIdInt, userCounter.toInt())
//            }
//        } catch (t: Throwable) {
//            log.error(t) { "Unhandled exception while handling component event" }
//            eventAdapter.onException(event, t)
//        }
//    }
//
//    private suspend fun handleTempView(
//        event: ModalInteractionEvent,
//        stateId: Long,
//        modalId: Int,
//        userCounter: Int,
//    ) {
//        val state = listener.messageCache.getIfPresent(stateId)
//            ?: return eventAdapter.onUnknownView(event)
////        state.processModal(event, modalId, userCounter)
//    }
//
//    private suspend fun handlePersistentView(
//        event: ModalInteractionEvent,
//        stateId: Long,
//        modalId: Int,
//        userCounter: Int
//    ) {
////        val state = listener.loadPersistentMessage(stateId)
////        state.processModal(event, modalId, userCounter)
//    }
//}