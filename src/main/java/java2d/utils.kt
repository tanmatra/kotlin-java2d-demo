package java2d

import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Image
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.image.BufferedImage
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

@Deprecated("Use GBC(x, y).fill()")
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
inline infix fun Int.hasBits(bits: Int): Boolean = (this and bits) != 0

@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.hasNoBits(bits: Int): Boolean = (this and bits) == 0

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

fun sqr(d: Double): Double = d * d

fun DoubleArray.distance() = Math.sqrt(sumByDouble { sqr(it) })

inline fun DoubleArray.replaceAll(transform: (Double) -> Double) {
    for (i in indices) {
        this[i] = transform(this[i])
    }
}

fun DoubleArray.normalize() {
    val distance = distance()
    replaceAll { it / distance }
}

fun Image.toBufferedImage(): BufferedImage {
    return if (this is BufferedImage) {
        this
    } else {
        BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_RGB).also {
            it.createGraphics().use { g ->
                g.drawImage(this, 0, 0, null)
            }
        }
    }
}

fun BufferedImage.createSimilar() = BufferedImage(width, height, type)

fun BufferedImage.copy(): BufferedImage = createSimilar().also {
    it.createGraphics().use { g ->
        g.drawImage(this, 0, 0, null)
    }
}

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE, initializer)
}
