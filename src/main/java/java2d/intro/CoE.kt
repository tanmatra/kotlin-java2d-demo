package java2d.intro

import java2d.copy
import java2d.hasBits
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.concurrent.ThreadLocalRandom

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
    private var zoom: Double = 0.0
    private var extent: Double = 0.0
    private val zIncr: Double = - (2.0 / (end - begin))
    private val eIncr: Double = 360.0 / (end - begin)
    private val doRandom: Boolean = type hasBits RAND

    override fun reset(surface: Intro.Surface, newWidth: Int, newHeight: Int) {
        if (doRandom) {
            val num = ThreadLocalRandom.current().nextInt(6)
            type = when (num) {
                0 -> OVAL
                1 -> RECT
                2 -> RECT or WID
                3 -> RECT or HEI
                4 -> ARC
                5 -> OVAL
                else -> error(6)
            }
        }
        shape = null
        bimg = null
        extent = 360.0
        zoom = 2.0
    }

    override fun step(surfaceImage: BufferedImage, surface: Intro.Surface, w: Int, h: Int) {
        if (bimg == null) {
            bimg = surfaceImage.copy()
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
        const val WID  =  1
        const val HEI  =  2
        const val OVAL =  4
        const val RECT =  8
        const val RAND = 16
        const val ARC  = 32
    }
}
