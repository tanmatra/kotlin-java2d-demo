package java2d

import javax.swing.AbstractButton
import javax.swing.Action
import javax.swing.JComboBox
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

fun Action.selectedProperty() = object : ReadWriteProperty<Any, Boolean>
{
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return isSelected
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        isSelected = value
    }
}

class ComboBoxSelectedIndexProperty(private val comboBox: JComboBox<*>) : ReadWriteProperty<Any, Int>
{
    override operator fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return comboBox.selectedIndex
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        comboBox.selectedIndex = value
    }
}

fun JComboBox<*>.selectedIndexProperty() = ComboBoxSelectedIndexProperty(this)
