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
    private val siw: Int,
    private val sih: Int,
    override val begin: Int,
    override val end: Int
) : Part
{
    private var bimg: BufferedImage? = null
    private val rIncr: Double = 360.0 / (end - begin)
    private val sIncr: Double = 1.0 / (end - begin)
    private var scale: Double = 0.0
    private var rotate: Double = 0.0
    private val subs = ArrayList<BufferedImage>(20)
    private val pts = ArrayList<Point>(20)

    override fun reset(newWidth: Int, newHeight: Int) {
        scale = 1.0
        rotate = 0.0
        bimg = null
        subs.clear()
        pts.clear()
    }

    override fun step(w: Int, h: Int) {
        if (bimg == null) {
            bimg = Intro.Surface.bufferedImage!!.createSimilar().also { bimg ->
                bimg.createGraphics().use { big ->
                    big.drawImage(Intro.Surface.bufferedImage, 0, 0, null)
                    run {
                        var x = 0
                        while (x < w && scale > 0.0) {
                            val ww = if (x + siw < w) siw else w - x
                            run {
                                var y = 0
                                while (y < h) {
                                    val hh = if (y + sih < h) sih else h - y
                                    subs.add(bimg.getSubimage(x, y, ww, hh))
                                    pts.add(Point(x, y))
                                    y += sih
                                }
                            }
                            x += siw
                        }
                    }
                }
            }
        }

        rotate += rIncr
        scale -= sIncr
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val saveTx = g2.transform
        g2.color = Intro.myBlue
        var i = 0
        while (i < subs.size && scale > 0.0) {
            val bi = subs[i]
            val p = pts[i]
            val ww = bi.width
            val hh = bi.height
            val at = AffineTransform()
            at.rotate(Math.toRadians(rotate), (p.x + ww / 2).toDouble(), (p.y + hh / 2).toDouble())
            at.translate(p.x.toDouble(), p.y.toDouble())
            at.scale(scale, scale)

            val b1 = Rectangle(0, 0, ww, hh)
            val shape = at.createTransformedShape(b1)
            val b2 = shape.bounds2D
            val xx = p.x + ww / 2 - (b2.x + b2.width / 2)
            val yy = p.y + hh / 2 - (b2.y + b2.height / 2)
            val toCenterAT = AffineTransform()
            toCenterAT.translate(xx, yy)
            toCenterAT.concatenate(at)

            g2.transform = toCenterAT
            g2.drawImage(bi, 0, 0, null)
            g2.draw(b1)
            i++
        }
        g2.transform = saveTx
    }
}
