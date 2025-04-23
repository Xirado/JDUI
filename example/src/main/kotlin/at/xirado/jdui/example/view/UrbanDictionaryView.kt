package at.xirado.jdui.example.view

import at.xirado.jdui.component.message.*
import at.xirado.jdui.component.row
import at.xirado.jdui.state.state
import at.xirado.jdui.view.View
import at.xirado.jdui.view.compose
import dev.minn.jda.ktx.util.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.TimeFormat
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

class UrbanDictionaryView : View() {
    private val httpClient: OkHttpClient by context
    private val query: UrbanDictionaryQuery? by state(context.get())
    private var pageIndex: Int by state(0)
    private var definitions: List<UrbanDefinition> by state(emptyList())

    init {
        require(query != null) { "UrbanDictionaryQuery not present in context!" }
    }

    override suspend fun initialize() {
        val query = this.query ?: throw IllegalStateException("Query not present in context!")

        if (definitions.isEmpty()) {
            val response = loadDefinitions(query.term, httpClient)
            definitions = response?.list ?: emptyList()
        }
    }

    override suspend fun createView() = compose {
        +container(0x1D2439) {
            val current = definitions.getOrNull(pageIndex)

            if (current == null) {
                +text("Found no definitions :(")
                return@container
            }

            +section(accessory = thumbnail("https://bean.bz/assets/udlogo.png")) {
                +text("## [${current.word}](${current.permalink})")
                +text(current.definition.processDefinitionHyperlinks())
            }
            +separator(true, Separator.Spacing.SMALL)

            current.example?.let {
                val example = "*$it*".processDefinitionHyperlinks()
                +text(example)
            }

            val writtenOn = Instant.parse(current.writtenOn)
            +text("-# \uD83D\uDC4D **${current.thumbsUp}** \uD83D\uDC4E **${current.thumbsDown}**")
            +text("-# by **${current.author}**, ${TimeFormat.RELATIVE.format(writtenOn)}")

            +separator(true, Separator.Spacing.SMALL)
            +text("-# Definition **${pageIndex + 1}** / **${definitions.size}**")
            +row {
                +button(ButtonStyle.SECONDARY, emoji = Emoji.fromUnicode("⬅\uFE0F"), disabled = pageIndex == 0) {
                    pageIndex--
                }
                +button(ButtonStyle.SECONDARY, emoji = Emoji.fromUnicode("➡\uFE0F"), disabled = pageIndex == definitions.lastIndex) {
                    pageIndex++
                }
            }
        }
    }
}

private val json = Json {
    ignoreUnknownKeys = true
}

private val definitionHyperlinkRegex = """\[([^]]+)]""".toRegex()

private suspend fun loadDefinitions(term: String, client: OkHttpClient): UrbanDefinitionResponse? {
    val url = HttpUrl.Builder()
        .scheme("https")
        .host("api.urbandictionary.com")
        .addPathSegments("/v0/define")
        .addQueryParameter("term", term)
        .build()

    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    return client.newCall(request).await().use { response ->
        if (!response.isSuccessful)
            throw IllegalStateException("API down? Got response with status ${response.code}")

        response.body?.let { json.decodeFromString<UrbanDefinitionResponse>(it.string()) }
            ?: throw IllegalStateException("Response does not have a body!")
    }
}

@Serializable
data class UrbanDictionaryQuery(val term: String)

@Serializable
data class UrbanDefinition(
    val word: String,
    val definition: String,
    val example: String?,
    val author: String,
    @SerialName("written_on")
    val writtenOn: String,
    val permalink: String,
    @SerialName("defid")
    val defId: Int,
    @SerialName("thumbs_up")
    val thumbsUp: Int,
    @SerialName("thumbs_down")
    val thumbsDown: Int,
)

@Serializable
data class UrbanDefinitionResponse(
    val list: List<UrbanDefinition>
)

private fun String.processDefinitionHyperlinks() = definitionHyperlinkRegex.replace(this) { match ->
    val (term) = match.destructured
    val encoded = URLEncoder.encode(term, StandardCharsets.UTF_8)

    val url = "https://www.urbandictionary.com/define.php?term=$encoded"

    "[$term]($url)"
}
