package java2d

import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.RenderingHints
import java.util.logging.Logger
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.JToggleButton

fun JComponent.restrictHeight(height: Int) {
    val prefSize = Dimension(preferredSize.width, height)
    preferredSize = prefSize
    maximumSize = prefSize
    minimumSize = prefSize
}

fun createToolButton(text: String,
                     state: Boolean,
                     toolTipText: String? = null,
                     action: (selected: Boolean) -> Unit
): AbstractButton {
    return JToggleButton(text).apply {
        isFocusPainted = false
        if (toolTipText != null) this.toolTipText = toolTipText
        isSelected = state
        restrictHeight(21)
        addActionListener { action(isSelected) }
    }
}

inline fun <reified T> getLogger() = Logger.getLogger(T::class.java.name)

fun byteArrayFrom(vararg ints: Int) = ByteArray(ints.size) { i -> ints[i].toByte() }

inline fun Graphics2D.use(block: (Graphics2D) -> Unit) {
    try {
        block(this)
    } finally {
        dispose()
    }
}

var Graphics2D.antialiasing: Boolean
    get() = getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON
    set(value) {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            if (value) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF)
    }

fun JComponent.addToGridBag(
    component: Component,
    x: Int, y: Int,
    w: Int, h: Int,
    weightx: Double, weighty: Double
) {
    val layout = layout as GridBagLayout
    val gbc = GridBagConstraints().apply gbc@{
        fill = GridBagConstraints.BOTH
        gridx = x
        gridy = y
        gridwidth = w
        gridheight = h
        this@gbc.weightx = weightx
        this@gbc.weighty = weighty
    }
    add(component)
    layout.setConstraints(component, gbc)
}
