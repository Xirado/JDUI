package at.xirado.jdui.component.message

import at.xirado.jdui.component.StatelessComponent
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.filedisplay.FileDisplay as JDAFileDisplay

class FileDisplay(var fileUpload: FileUpload) : StatelessComponent<JDAFileDisplay>() {
    override fun buildComponent(uniqueId: Int): JDAFileDisplay {
        return JDAFileDisplay.fromFile(fileUpload)
            .withUniqueId(uniqueId)

    }

    override val type = typeOf<JDAFileDisplay>()
}

fun fileDisplay(
    file: FileUpload
): FileDisplay {
    return FileDisplay(file)
}