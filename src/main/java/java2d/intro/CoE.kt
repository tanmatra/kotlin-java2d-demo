package java2d.intro

import java2d.copy
import java2d.hasBits
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

/**
 * Close out effect.  Close out the buffered image with different geometry shapes.
 */
internal class CoE(
    private var type: Int,
    override val begin: Int,
    override val end: Int
) : Part
{
    private var bimg: BufferedImage? = null
    private var shape: Shape? = null
    private var zoom: Double = 0.toDouble()
    private var extent: Double = 0.toDouble()
    private val zIncr: Double = - (2.0 / (end - begin))
    private val eIncr: Double = 360.0 / (end - begin)
    private val doRandom: Boolean = type and RAND != 0

    override fun reset(newWidth: Int, newHeight: Int) {
        if (doRandom) {
            val num = (Math.random() * 5.0).toInt()
            type = when (num) {
                0 -> OVAL
                1 -> RECT
                2 -> RECT or WID
                3 -> RECT or HEI
                4 -> ARC
                else -> OVAL
            }
        }
        shape = null
        bimg = null
        extent = 360.0
        zoom = 2.0
    }

    override fun step(w: Int, h: Int) {
        if (bimg == null) {
            bimg = Intro.Surface.bufferedImage!!.copy()
        }
        val z = Math.min(w, h) * zoom
        shape = when {
            type hasBits OVAL -> Ellipse2D.Double(w / 2 - z / 2, h / 2 - z / 2, z, z)
            type hasBits ARC -> Arc2D.Double(-100.0, -100.0, w + 200.0, h + 200.0, 90.0, extent, Arc2D.PIE)
                .also { extent -= eIncr }
            type hasBits RECT -> when {
                type hasBits WID -> Rectangle2D.Double(w / 2 - z / 2, 0.0, z, h.toDouble())
                type hasBits HEI -> Rectangle2D.Double(0.0, h / 2 - z / 2, w.toDouble(), z)
                else -> Rectangle2D.Double(w / 2 - z / 2, h / 2 - z / 2, z, z)
            }
            else -> shape // ?
        }
        zoom += zIncr
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.clip(shape)
        g2.drawImage(bimg, 0, 0, null)
    }

    companion object
    {
        const val WID = 1
        const val HEI = 2
        const val OVAL = 4
        const val RECT = 8
        const val RAND = 16
        const val ARC = 32
    }
}
