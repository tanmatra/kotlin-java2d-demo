package java2d.intro

import java.awt.AlphaComposite
import java.awt.Composite
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Paint
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath

/**
 * Text Effect.  Transformation of characters.  Clip or fill.
 */
internal class TxE(
    text: String,
    font: Font,
    private val type: Int,
    private val paint: Paint?,
    override val begin: Int,
    override val end: Int
) : Part
{
    private var rIncr: Double = 0.toDouble()
    private var sIncr: Double = 0.toDouble()
    private var sx: Double = 0.toDouble()
    private var sy: Double = 0.toDouble()
    private var rotate: Double = 0.toDouble()
    private val shapes: Array<Shape>
    private val txShapes: Array<Shape?>
    private val sw: Int
    private var numRev: Int = 0

    init {
        setIncrements(2.0)
        val chars = text.toCharArray()
        txShapes = arrayOfNulls(chars.size)
        val frc = FontRenderContext(null, true, true)
        val tl = TextLayout(text, font, frc)
        sw = tl.getOutline(null).bounds.getWidth().toInt()
        shapes = Array(chars.size) { j ->
            val s = chars[j].toString()
            TextLayout(s, font, frc).getOutline(null)
        }
    }

    private fun setIncrements(numRevolutions: Double) {
        this.numRev = numRevolutions.toInt()
        rIncr = 360.0 / ((end - begin) / numRevolutions)
        sIncr = 1.0 / (end - begin)
        if (type and SCX != 0 || type and SCY != 0) {
            sIncr *= 2.0
        }
        if (type and DEC != 0) {
            rIncr = -rIncr
            sIncr = -sIncr
        }
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        when (type) {
            SCXI -> {
                sx = -1.0
                sy = 1.0
            }
            SCYI -> {
                sx = 1.0
                sy = -1.0
            }
            else -> {
                sy = if (type and DEC != 0) 1.0 else 0.0
                sx = sy
            }
        }
        rotate = 0.0
    }

    override fun step(w: Int, h: Int) {
        var charWidth = (w / 2 - sw / 2).toFloat()

        for (i in shapes.indices) {
            val at = AffineTransform()
            val maxBounds = shapes[i].bounds
            at.translate(charWidth.toDouble(), h / 2 + maxBounds.getHeight() / 2)
            charWidth += maxBounds.getWidth().toFloat() + 1
            var shape = at.createTransformedShape(shapes[i])
            val b1 = shape.bounds2D

            if (type and R != 0) {
                at.rotate(Math.toRadians(rotate))
            }
            if (type and SC != 0) {
                at.scale(sx, sy)
            }
            shape = at.createTransformedShape(shapes[i])
            val b2 = shape.bounds2D

            val xx = b1.x + b1.width / 2 - (b2.x + b2.width / 2)
            val yy = b1.y + b1.height / 2 - (b2.y + b2.height / 2)
            val toCenterAT = AffineTransform()
            toCenterAT.translate(xx, yy)
            toCenterAT.concatenate(at)
            txShapes[i] = toCenterAT.createTransformedShape(shapes[i])
        }
        // avoid over rotation
        if (Math.abs(rotate) <= numRev * 360) {
            rotate += rIncr
            when {
                type and SCX != 0 -> sx += sIncr
                type and SCY != 0 -> sy += sIncr
                else -> {
                    sx += sIncr
                    sy += sIncr
                }
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        var saveAC: Composite? = null
        if (type and AC != 0 && sx > 0 && sx < 1) {
            saveAC = g2.composite
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sx.toFloat())
        }
        var path: GeneralPath? = null
        if (type and CLIP != 0) {
            path = GeneralPath()
        }
        if (paint != null) {
            g2.paint = paint
        }
        for (i in txShapes.indices) {
            if (type and CLIP != 0) {
                path!!.append(txShapes[i], false)
            } else {
                g2.fill(txShapes[i])
            }
        }
        if (type and CLIP != 0) {
            g2.clip(path)
        }
        if (saveAC != null) {
            g2.composite = saveAC
        }
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object
    {
        const val INC = 1
        const val DEC = 2
        const val R = 4            // rotate
        const val RI = R or INC
        const val RD = R or DEC
        const val SC = 8            // scale
        const val SCI = SC or INC
        const val SCD = SC or DEC
        const val SCX = 16           // scale invert x
        const val SCXI = SCX or SC or INC
        const val SCXD = SCX or SC or DEC
        const val SCY = 32           // scale invert y
        const val SCYI = SCY or SC or INC
        const val SCYD = SCY or SC or DEC
        const val AC = 64           // AlphaComposite
        const val CLIP = 128          // Clipping
        const val NOP = 512          // No Paint
    }
}
