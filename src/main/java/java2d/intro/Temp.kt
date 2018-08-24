package java2d.intro

import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle

/**
 * Template for Features & Contributors consisting of translating
 * blue and red rectangles and an image going from transparent to
 * opaque.
 */
internal class Temp(
    private val type: Int,
    private val img: Image?,
    override val begin: Int,
    override val end: Int
) : Part {
    private var alpha: Float = 0.toFloat()
    private val aIncr: Float = 0.9f / (end - begin)
    private var rect1: Rectangle? = null
    private var rect2: Rectangle? = null
    private var x: Int = 0
    private var y: Int = 0
    private var xIncr: Int = 0
    private var yIncr: Int = 0

    init {
        if (type and NOANIM != 0) {
            alpha = 1.0f
        }
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        rect1 = Rectangle(8, 20, newWidth - 20, 30)
        rect2 = Rectangle(20, 8, 30, newHeight - 20)
        if (type and NOANIM == 0) {
            alpha = 0.0f
            xIncr = newWidth / (end - begin)
            yIncr = newHeight / (end - begin)
            x = newWidth + (xIncr * 1.4).toInt()
            y = newHeight + (yIncr * 1.4).toInt()
        }
    }

    override fun step(w: Int, h: Int) {
        if (type and NOANIM != 0) {
            return
        }
        if (type and RECT != 0) {
            x -= xIncr
            rect1!!.setLocation(x, 20)
            y -= yIncr
            rect2!!.setLocation(20, y)
        }
        if (type and IMG != 0) {
            alpha += aIncr
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        if (type and RECT != 0) {
            g2.color = Intro.myBlue
            g2.fill(rect1)
            g2.color = Intro.myRed
            g2.fill(rect2)
        }
        if (type and IMG != 0) {
            val saveAC = g2.composite
            if (alpha in 0.0 .. 1.0) {
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            }
            g2.drawImage(img, 30, 30, null)
            g2.composite = saveAC
        }
    }

    companion object
    {
        const val NOANIM = 1
        const val RECT = 2
        const val IMG = 4
        const val RNA = RECT or NOANIM
        const val INA = IMG or NOANIM
    }
}
