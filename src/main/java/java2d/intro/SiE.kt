package java2d.intro

import java2d.createSimilar
import java2d.use
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.util.ArrayList

/**
 * Subimage effect.  Subimage the scene's buffered
 * image then rotate and scale down the subimages.
 */
internal class SiE(
    private val subimageWidth: Int,
    private val subimageHeight: Int,
    override val begin: Int,
    override val end: Int
) : Part
{
    private var bufferedImage: BufferedImage? = null
    private val rotateIncr: Double = 360.0 / (end - begin)
    private val scaleIncr: Double = 1.0 / (end - begin)
    private var scale: Double = 0.0
    private var rotate: Double = 0.0
    private val subimages = ArrayList<BufferedImage>(20)
    private val points = ArrayList<Point>(20)

    override fun reset(newWidth: Int, newHeight: Int) {
        scale = 1.0
        rotate = 0.0
        bufferedImage = null
        subimages.clear()
        points.clear()
    }

    override fun step(w: Int, h: Int) {
        if (bufferedImage == null) {
            bufferedImage = Intro.Surface.bufferedImage!!.createSimilar().also { image ->
                image.createGraphics().use { big ->
                    big.drawImage(Intro.Surface.bufferedImage, 0, 0, null)
                    for (x in 0 until w step subimageWidth) {
                        if (scale <= 0.0) break
                        val ww = if (x + subimageWidth < w) subimageWidth else w - x
                        for (y in 0 until h step subimageHeight) {
                            val hh = if (y + subimageHeight < h) subimageHeight else h - y
                            subimages.add(image.getSubimage(x, y, ww, hh))
                            points.add(Point(x, y))
                        }
                    }
                }
            }
        }
        rotate += rotateIncr
        scale -= scaleIncr
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val saveTx = g2.transform
        g2.color = Intro.myBlue
        for (i in subimages.indices) {
            if (scale <= 0.0) break
            val bi = subimages[i]
            val p = points[i]
            val ww = bi.width
            val hh = bi.height
            val at = AffineTransform().apply {
                rotate(Math.toRadians(rotate), (p.x + ww / 2).toDouble(), (p.y + hh / 2).toDouble())
                translate(p.x.toDouble(), p.y.toDouble())
                scale(scale, scale)
            }
            val b1 = Rectangle(0, 0, ww, hh)
            val shape = at.createTransformedShape(b1)
            val b2 = shape.bounds2D
            val xx = p.x + ww / 2 - (b2.x + b2.width / 2)
            val yy = p.y + hh / 2 - (b2.y + b2.height / 2)
            g2.transform = AffineTransform().apply {
                translate(xx, yy)
                concatenate(at)
            }
            g2.drawImage(bi, 0, 0, null)
            g2.draw(b1)
        }
        g2.transform = saveTx
    }
}
