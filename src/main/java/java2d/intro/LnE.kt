package java2d.intro

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Composite
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.FlatteningPathIterator
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.util.ArrayList

/**
 * Line Effect.  Flattened ellipse with lines from the center
 * to the edge.  Expand or collapse the ellipse.  Fade in or out
 * the lines.
 */
internal class LnE(
    private val type: Int,
    override val begin: Int,
    override val end: Int
) : Part {
    private var rIncr: Double = 0.0
    private var rotate: Double = 0.0
    private var zIncr: Double = 0.0
    private var zoom: Double = 0.0
    private val pts = ArrayList<Point2D.Double>()
    private var alpha: Float = 0.0f
    private var aIncr: Float = 0.0f

    init {
        val range = (this.end - begin).toFloat()
        rIncr = (360.0f / range).toDouble()
        aIncr = 0.9f / range
        zIncr = (2.0f / range).toDouble()
        if (type and DEC != 0) {
            rIncr = -rIncr
            aIncr = -aIncr
            zIncr = -zIncr
        }
    }

    private fun generatePts(w: Int, h: Int, sizeF: Double) {
        pts.clear()
        val size = Math.min(w, h) * sizeF
        val ellipse = Ellipse2D.Double(w / 2 - size / 2, h / 2 - size / 2, size, size)
        val pi = ellipse.getPathIterator(null, 0.8)
        while (!pi.isDone) {
            val pt = DoubleArray(6)
            when (pi.currentSegment(pt)) {
                FlatteningPathIterator.SEG_MOVETO, FlatteningPathIterator.SEG_LINETO ->
                    pts.add(Point2D.Double(pt[0], pt[1]))
            }
            pi.next()
        }
    }

    override fun reset(surface: Intro.Surface, newWidth: Int, newHeight: Int) {
        if (type and DEC != 0) {
            rotate = 360.0
            alpha = 1.0f
            zoom = 2.0
        } else {
            alpha = 0f
            rotate = alpha.toDouble()
            zoom = 0.0
        }
        if (type and ZOOM == 0) {
            generatePts(newWidth, newHeight, 0.5)
        }
    }

    override fun step(surface: Intro.Surface, w: Int, h: Int) {
        if (type and ZOOM != 0) {
            zoom += zIncr
            generatePts(w, h, zoom)
        }
        if (type and RI != 0 || type and RI != 0) {
            rotate += rIncr
        }
        if (type and ACI != 0 || type and ACD != 0) {
            alpha += aIncr
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        var saveAC: Composite? = null
        if (type and AC != 0 && alpha >= 0 && alpha <= 1) {
            saveAC = g2.composite
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        }
        var saveTx: AffineTransform? = null
        if (type and R != 0) {
            saveTx = g2.transform
            val at = AffineTransform()
            at.rotate(Math.toRadians(rotate), (w / 2).toDouble(), (h / 2).toDouble())
            g2.transform = at
        }
        val p1 = Point2D.Double((w / 2).toDouble(), (h / 2).toDouble())
        g2.color = Color.YELLOW
        for (pt in pts) {
            g2.draw(Line2D.Float(p1, pt))
        }
        if (saveTx != null) {
            g2.transform = saveTx
        }
        if (saveAC != null) {
            g2.composite = saveAC
        }
    }

    companion object
    {
        const val INC = 1
        const val DEC = 2
        const val R = 4             // rotate
        const val ZOOM = 8             // zoom
        const val AC = 32             // AlphaComposite
        const val RI = R or INC
        const val RD = R or DEC
        const val ZOOMI = ZOOM or INC
        const val ZOOMD = ZOOM or DEC
        const val ACI = AC or INC
        const val ACD = AC or DEC
    }
}
