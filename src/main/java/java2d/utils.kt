package java2d

import java.awt.Dimension
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
