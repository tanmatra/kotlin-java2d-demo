package java2d

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

interface DemoOptions
{
    var toolBar: Boolean
    var antialiasing: Boolean
    var renderQuality: Boolean
    var texture: Boolean
    var composite: Boolean

    fun copyFrom(other: DemoOptions, eventSource: KProperty<*>?) {
        if (eventSource == null) {
            toolBar = other.toolBar
            antialiasing = other.antialiasing
            renderQuality = other.renderQuality
            texture = other.texture
            composite = other.composite
        } else {
            val property = PROPERTIES.find { it.name == eventSource.name } ?: return
            property.set(this, property.get(other))
        }
    }

    companion object
    {
        private val PROPERTIES: List<KMutableProperty1<DemoOptions, Boolean>> = listOf(
            DemoOptions::toolBar,
            DemoOptions::antialiasing,
            DemoOptions::renderQuality,
            DemoOptions::texture,
            DemoOptions::composite)
    }
}
