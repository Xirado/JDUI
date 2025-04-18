package at.xirado.jdui.component.message

import at.xirado.jdui.component.StatelessComponent
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.thumbnail.Thumbnail as JDAThumbnail

class Thumbnail(val fileUpload: FileUpload?, val url: String?) : StatelessComponent<JDAThumbnail>() {
    init {
        require(fileUpload != null || url != null) { "Thumbnail requires either FileUpload or url to be set!" }
        require(!(fileUpload != null && url != null)) { "Cannot set both FileUpload and url in Thumbnail" }
    }

    override fun buildComponent(uniqueId: Int): JDAThumbnail {
        return fileUpload?.let {
            JDAThumbnail.fromFile(it).withUniqueId(uniqueId)
        } ?: JDAThumbnail.fromUrl(url!!).withUniqueId(uniqueId)
    }

    override val type = typeOf<JDAThumbnail>()
}

fun thumbnail(file: FileUpload) = Thumbnail(file, null)
fun thumbnail(url: String) = Thumbnail(null, url)