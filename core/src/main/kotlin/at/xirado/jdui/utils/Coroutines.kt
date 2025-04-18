package at.xirado.jdui.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.requests.RestAction
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun <reified T> newCoroutineScope(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    job: Job = SupervisorJob(),
    context: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope {
    val logger = KotlinLogging.logger(T::class.java.name)
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error(throwable) { "Uncaught exception from coroutine" }
        if (throwable is Error) {
            job.cancel()
            throw throwable
        }
    }

    return CoroutineScope(dispatcher + job + errorHandler + context)
}

fun newCoroutineScope(
    clazzName: String,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    job: Job = SupervisorJob(),
    context: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope {
    val logger = KotlinLogging.logger(clazzName)
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error(throwable) { "Uncaught exception from coroutine" }
        if (throwable is Error) {
            job.cancel()
            throw throwable
        }
    }

    return CoroutineScope(dispatcher + job + errorHandler + context)
}

suspend fun <T> RestAction<T>.await(): T = submit().await()