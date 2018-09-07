package java2d

import javax.swing.AbstractButton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SelectedProperty(private val button: AbstractButton) : ReadWriteProperty<Any, Boolean>
{
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return button.isSelected
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        button.isSelected = value
    }
}

inline val AbstractButton.isSelectedProperty get() = SelectedProperty(this)
