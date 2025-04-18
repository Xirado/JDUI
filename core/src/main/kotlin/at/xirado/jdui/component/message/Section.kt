package at.xirado.jdui.component.message

import at.xirado.jdui.component.AccessoryComponentContainer
import net.dv8tion.jda.api.components.section.SectionContentComponent
import kotlin.reflect.typeOf
import net.dv8tion.jda.api.components.section.Section as JDASection
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent as JDASectionAccessoryComponent

class Section : AccessoryComponentContainer<JDASection, SectionContentComponent, JDASectionAccessoryComponent>() {
    override fun build(
        uniqueId: Int,
        children: Collection<SectionContentComponent>,
        accessory: JDASectionAccessoryComponent?
    ): JDASection {
        if (accessory == null)
            throw IllegalStateException("Cannot build Section without accessory!")

        return JDASection.of(accessory, children).withUniqueId(uniqueId)
    }

    override val type = typeOf<JDASection>()
}

fun section(
    block: Section.() -> Unit
): Section {
    return Section().apply(block)
}