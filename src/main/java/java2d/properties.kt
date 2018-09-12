package java2d

import javax.swing.AbstractButton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias KPropertyCallback = (KProperty<*>) -> Unit

class SelectedProperty(
    private val button: AbstractButton,
    private val onChange: KPropertyCallback? = null
) : ReadWriteProperty<Any, Boolean>
{
    override operator fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return button.isSelected
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        button.isSelected = value
    }

    operator fun provideDelegate(thisRef: Any, kProperty: KProperty<*>): ReadWriteProperty<Any, Boolean> {
        if (onChange != null) {
            button.addItemListener {
                onChange.invoke(kProperty)
            }
        }
        return this
    }
}

fun AbstractButton.selectedProperty(onChange: KPropertyCallback? = null) = SelectedProperty(this, onChange)
