package at.xirado.jdui.example.view

import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.mediaGallery
import at.xirado.jdui.component.row
import at.xirado.jdui.state.state
import at.xirado.jdui.view.View
import at.xirado.jdui.view.compose
import dev.minn.jda.ktx.util.await
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.dv8tion.jda.api.components.buttons.ButtonStyle.SECONDARY
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class Cat(val id: String, val url: String, val width: Int, val height: Int)

private const val CAT_URL = "https://api.thecatapi.com/v1/images/search?limit=10"

class CatView : View() {
    private val httpClient: OkHttpClient by context
    private val cats: MutableList<Cat> by state(mutableListOf())

    private var currentCatJob: Deferred<Unit>? = null

    override suspend fun initialize() {
        if (cats.isEmpty())
            fetchMoreCats()
    }

    override suspend fun createView() = compose {
        +container(0x328fa8) {
            +mediaGallery {
                +MediaGalleryItem.fromUrl(cats[0].url)
            }
            +row {
                +button(SECONDARY, "Generate cat") {
                    cats.removeFirst()
                    if (cats.size == 1 && currentCatJob == null) {
                        currentCatJob = coroutineScope.async {
                            fetchMoreCats()
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchMoreCats() {
        val newCats = getCats(httpClient)
        // Ensures proper synchronization
        locked {
            cats += newCats
            currentCatJob = null
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun getCats(client: OkHttpClient): List<Cat> {
    val request = Request.Builder()
        .url(CAT_URL)
        .get()
        .build()

    val response = client.newCall(request).await()

    return response.use {
        val stream = it.body!!.byteStream()
        Json.Default.decodeFromStream(stream)
    }
}