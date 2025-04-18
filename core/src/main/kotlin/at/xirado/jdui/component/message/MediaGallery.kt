package at.xirado.jdui.component.message

import at.xirado.jdui.component.StatelessComponent
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.mediagallery.MediaGallery as JDAMediaGallery

class MediaGallery : StatelessComponent<JDAMediaGallery>() {
    private val items = mutableListOf<MediaGalleryItem>()

    override fun buildComponent(uniqueId: Int): JDAMediaGallery {
        return JDAMediaGallery.of(items)
            .withUniqueId(uniqueId)
    }

    operator fun MediaGalleryItem.unaryPlus() {
        items += this
    }

    override val type = typeOf<JDAMediaGallery>()
}

fun mediaGallery(
    block: MediaGallery.() -> Unit
): MediaGallery {
    return MediaGallery().apply(block)
}