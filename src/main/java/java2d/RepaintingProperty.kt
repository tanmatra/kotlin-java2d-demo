package java2d

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RepaintingProperty<T>(private var value: T) : ReadWriteProperty<AnimatingSurface, T>
{
    override fun getValue(thisRef: AnimatingSurface, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: AnimatingSurface, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.checkRepaint()
    }
}
