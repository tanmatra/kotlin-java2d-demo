package java2d

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RepaintingProperty<T>(private var value: T) : ReadWriteProperty<Surface, T>
{
    override fun getValue(thisRef: Surface, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Surface, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.checkRepaint()
    }
}
