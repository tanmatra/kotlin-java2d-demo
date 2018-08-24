package java2d.intro

import java.awt.Graphics2D
import java.awt.Paint
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

/**
 * TexturePaint Effect.  Expand and collapse a texture.
 */
internal class TpE(
    private val type: Int,
    private val p1: Paint,
    private val p2: Paint,
    size: Int,
    override val begin: Int,
    override val end: Int
) : Part
{
    private var incr: Float = 0.0f
    private var index: Float = 0.0f
    private var texture: TexturePaint? = null
    private var size: Int = 0
    private var bimg: BufferedImage? = null
    private var rect: Rectangle? = null

    init {
        setTextureSize(size)
    }

    private fun setTextureSize(size: Int) {
        this.size = size
        bimg = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        rect = Rectangle(0, 0, size, size)
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        incr = size.toFloat() / (end - begin).toFloat()
        if (type and HAF != 0) {
            incr /= 2f
        }
        if (type and DEC != 0) {
            index = size.toFloat()
            if (type and HAF != 0) {
                index /= 2f
            }
            incr = -incr
        } else {
            index = 0.0f
        }
        index += incr
    }

    override fun step(w: Int, h: Int) {
        val g2 = bimg!!.createGraphics()
        g2.paint = p1
        g2.fillRect(0, 0, size, size)
        g2.paint = p2
        if (type and OVAL != 0) {
            g2.fill(Ellipse2D.Float(0f, 0f, index, index))
        } else if (type and RECT != 0) {
            g2.fill(Rectangle2D.Float(0f, 0f, index, index))
        }
        texture = TexturePaint(bimg, rect!!)
        g2.dispose()
        index += incr
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.paint = texture
        if (type and NF == 0) {
            g2.fillRect(0, 0, w, h)
        }
    }

    companion object
    {
        const val INC  =  1 // increasing
        const val DEC  =  2 // decreasing
        const val OVAL =  4 // oval
        const val RECT =  8 // rectangle
        const val HAF  = 16 // half oval or rect size
        const val NF   = 32 // no fill
        const val OI = OVAL or INC
        const val OD = OVAL or DEC
        const val RI = RECT or INC
        const val RD = RECT or DEC
    }
}
