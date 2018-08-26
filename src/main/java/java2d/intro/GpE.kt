package java2d.intro

import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import java.util.ArrayList

/**
 * GradientPaint Effect.  Burst, split, horizontal and
 * vertical gradient fill effects.
 */
internal class GpE(
    private val type: Int,
    private val c1: Color,
    private val c2: Color,
    override val begin: Int,
    override val end: Int
) : Part
{
    private var incr: Float = 0.0f
    private var index: Float = 0.0f
    private val rect = ArrayList<Rectangle2D>()
    private val grad = ArrayList<GradientPaint>()

    override fun reset(newWidth: Int, newHeight: Int) {
        incr = 1.0f / (end - begin)
        if (type and CNT != 0) {
            incr /= 2.3f
        }
        if (type and CNT != 0 && type and INC != 0) {
            index = 0.5f
        } else if (type and DEC != 0) {
            index = 1.0f
            incr = -incr
        } else {
            index = 0.0f
        }
        index += incr
    }

    override fun step(w: Int, h: Int) {
        rect.clear()
        grad.clear()

        when {
            type and WID != 0 -> {
                val w2: Float
                val x1: Float
                val x2: Float
                if (type and SPL != 0) {
                    w2 = w * 0.5f
                    x1 = w * (1.0f - index)
                    x2 = w * index
                } else {
                    w2 = w * index
                    x2 = w2
                    x1 = x2
                }
                rect.add(Rectangle2D.Float(0f, 0f, w2, h.toFloat()))
                rect.add(Rectangle2D.Float(w2, 0f, w - w2, h.toFloat()))
                grad.add(GradientPaint(0f, 0f, c1, x1, 0f, c2))
                grad.add(GradientPaint(x2, 0f, c2, w.toFloat(), 0f, c1))
            }
            type and HEI != 0 -> {
                val h2: Float
                val y1: Float
                val y2: Float
                if (type and SPL != 0) {
                    h2 = h * 0.5f
                    y1 = h * (1.0f - index)
                    y2 = h * index
                } else {
                    h2 = h * index
                    y2 = h2
                    y1 = y2
                }
                rect.add(Rectangle2D.Float(0f, 0f, w.toFloat(), h2))
                rect.add(Rectangle2D.Float(0f, h2, w.toFloat(), h - h2))
                grad.add(GradientPaint(0f, 0f, c1, 0f, y1, c2))
                grad.add(GradientPaint(0f, y2, c2, 0f, h.toFloat(), c1))
            }
            type and BUR != 0 -> {
                val w2 = (w / 2).toFloat()
                val h2 = (h / 2).toFloat()

                rect.add(Rectangle2D.Float(0f, 0f, w2, h2))
                rect.add(Rectangle2D.Float(w2, 0f, w2, h2))
                rect.add(Rectangle2D.Float(0f, h2, w2, h2))
                rect.add(Rectangle2D.Float(w2, h2, w2, h2))

                val x1 = w * (1.0f - index)
                val x2 = w * index
                val y1 = h * (1.0f - index)
                val y2 = h * index

                grad.add(GradientPaint(0f, 0f, c1, x1, y1, c2))
                grad.add(GradientPaint(w.toFloat(), 0f, c1, x2, y1, c2))
                grad.add(GradientPaint(0f, h.toFloat(), c1, x1, y2, c2))
                grad.add(GradientPaint(w.toFloat(), h.toFloat(), c1, x2, y2, c2))
            }
            type and NF != 0 -> {
                val y = h * index
                grad.add(GradientPaint(0f, 0f, c1, 0f, y, c2))
            }
        }

        if (type and INC != 0 || type and DEC != 0) {
            index += incr
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        for (i in grad.indices) {
            g2.paint = grad[i]
            if (type and NF == 0) {
                g2.fill(rect[i])
            }
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    @Suppress("unused")
    companion object
    {
        const val INC = 1               // increasing
        const val DEC = 2               // decreasing
        const val CNT = 4               // center
        const val WID = 8               // width
        const val WI = WID or INC
        const val WD = WID or DEC
        const val HEI = 16              // height
        const val HI = HEI or INC
        const val HD = HEI or DEC
        const val SPL = 32 or CNT       // split
        const val SIW = SPL or INC or WID
        const val SDW = SPL or DEC or WID
        const val SIH = SPL or INC or HEI
        const val SDH = SPL or DEC or HEI
        const val BUR = 64 or CNT        // burst
        const val BURI = BUR or INC
        const val BURD = BUR or DEC
        const val NF = 128               // no fill
    }
}
