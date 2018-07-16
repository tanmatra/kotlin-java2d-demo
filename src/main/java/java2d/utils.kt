package java2d

import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.Toolkit
import java.lang.reflect.InvocationTargetException
import java.util.logging.Logger
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.JSlider
import javax.swing.JToggleButton
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder
import kotlin.reflect.KMutableProperty0

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
        margin = Insets(2, 2, 2, 2)
        restrictHeight(21)
        addActionListener { action(isSelected) }
    }
}

fun createBooleanButton(
    property: KMutableProperty0<Boolean>,
    text: String,
    toolTip: String? = null
): AbstractButton {
    return createToolButton(text, property.get(), toolTip) { selected ->
        property.set(selected)
    }
}

inline fun <reified T> getLogger() = Logger.getLogger(T::class.java.name)

fun byteArrayFrom(vararg ints: Int) = ByteArray(ints.size) { i -> ints[i].toByte() }

inline fun <T : Graphics> T.use(block: (T) -> Unit) {
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

var Graphics2D.textAntialiasing: Any?
    get() = getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING)
    set(value) = setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, value)

val systemTextAntialiasing: Any? = run {
    val hints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")
    (hints as? Map<*, *>)?.get(RenderingHints.KEY_TEXT_ANTIALIASING)
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

fun gbc() = GridBagConstraints()
fun gbc(x: Int, y: Int): GridBagConstraints = GridBagConstraints().apply {
    gridx = x
    gridy = y
}
fun GridBagConstraints.at(x: Int, y: Int): GridBagConstraints {
    gridx = x
    gridy = y
    return this
}
fun GridBagConstraints.fill(value: Int): GridBagConstraints {
    fill = value
    return this
}
fun GridBagConstraints.fillBoth(): GridBagConstraints {
    fill = GridBagConstraints.BOTH
    return this
}
fun GridBagConstraints.fillVertical(): GridBagConstraints {
    fill = GridBagConstraints.VERTICAL
    return this
}
fun GridBagConstraints.fillHorizontal(): GridBagConstraints {
    fill = GridBagConstraints.HORIZONTAL
    return this
}
fun GridBagConstraints.span(x: Int, y: Int): GridBagConstraints {
    gridwidth = x
    gridheight = y
    return this
}
fun GridBagConstraints.pad(x: Int, y: Int): GridBagConstraints {
    ipadx = x
    ipady = y
    return this
}
fun GridBagConstraints.weight(x: Double, y: Double): GridBagConstraints {
    weightx = x
    weighty = y
    return this
}
fun GridBagConstraints.insets(top: Int, left: Int, bottom: Int, right: Int): GridBagConstraints {
    insets = Insets(top, left, bottom, right)
    return this
}

fun <R> executeAndReturn(function: () -> R): R {
    if (EventQueue.isDispatchThread()) {
        return function()
    }
    var result: R? = null
    try {
        EventQueue.invokeAndWait {
            result = function()
        }
    } catch (e: InvocationTargetException) {
        throw e.targetException
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    }
    @Suppress("UNCHECKED_CAST")
    return result as R
}

fun String.rangeIndexOf(substring: String): IntRange {
    val i = indexOf(substring)
    return if (i < 0) IntRange.EMPTY else i .. (i + substring.length - 1)
}

infix fun IntRange.shift(offset: Int) = start + offset .. endInclusive + offset

val Insets.horizontal get() = left + right
val Insets.vertical get() = top + bottom

@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.hasBits(bits: Int) = (this and bits) != 0

fun createTitledSlider(suffix: String, max: Int, property: KMutableProperty0<Int>): JSlider {
    fun formatTitle(value: Int) = "$value $suffix"
    val titledBorder = TitledBorder(EtchedBorder()).apply {
        title = formatTitle(property.get())
    }
    return JSlider(SwingConstants.HORIZONTAL, 0, max, property.get()).apply {
        border = titledBorder
        isOpaque = true
        preferredSize = Dimension(150, 44) // (80, 44)
        addChangeListener {
            titledBorder.title = formatTitle(value)
            property.set(value)
            repaint()
        }
    }
}
