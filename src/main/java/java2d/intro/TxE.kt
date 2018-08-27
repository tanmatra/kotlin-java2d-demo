package java2d.intro

import java2d.hasBits
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
    private var rIncr: Double = 0.0
    private var sIncr: Double = 0.0
    private var sx: Double = 0.0
    private var sy: Double = 0.0
    private var rotate: Double = 0.0
    private val shapes: Array<Shape>
    private val txShapes: Array<Shape?>
    private val textWidth: Int
    private var numRev: Int = 0

    init {
        setIncrements(2.0)
        val chars = text.toCharArray()
        txShapes = arrayOfNulls(chars.size)
        val frc = FontRenderContext(null, true, true)
        val textLayout = TextLayout(text, font, frc)
        textWidth = textLayout.getOutline(null).bounds.getWidth().toInt()
        shapes = chars.map { c -> TextLayout(c.toString(), font, frc).getOutline(null) }
            .toTypedArray()
    }

    private fun setIncrements(numRevolutions: Double) {
        this.numRev = numRevolutions.toInt()
        rIncr = 360.0 / ((end - begin) / numRevolutions)
        sIncr = 1.0 / (end - begin)
        if (type hasBits SCX || type hasBits SCY) {
            sIncr *= 2.0
        }
        if (type hasBits DEC) {
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
                sy = if (type hasBits DEC) 1.0 else 0.0
                sx = sy
            }
        }
        rotate = 0.0
    }

    override fun step(w: Int, h: Int) {
        var charX = (w / 2 - textWidth / 2).toFloat()

        for (i in shapes.indices) {
            val at = AffineTransform()
            val maxBounds = shapes[i].bounds
            at.translate(charX.toDouble(), h / 2 + maxBounds.getHeight() / 2)
            charX += maxBounds.getWidth().toFloat() + 1
            var shape = at.createTransformedShape(shapes[i])
            val b1 = shape.bounds2D

            if (type hasBits R) {
                at.rotate(Math.toRadians(rotate))
            }
            if (type hasBits SC) {
                at.scale(sx, sy)
            }
            shape = at.createTransformedShape(shapes[i])
            val b2 = shape.bounds2D

            val xx = b1.x + b1.width / 2 - (b2.x + b2.width / 2)
            val yy = b1.y + b1.height / 2 - (b2.y + b2.height / 2)
            val toCenterAT = AffineTransform().apply {
                translate(xx, yy)
                concatenate(at)
            }
            txShapes[i] = toCenterAT.createTransformedShape(shapes[i])
        }
        // avoid over rotation
        if (Math.abs(rotate) <= numRev * 360) {
            rotate += rIncr
            when {
                type hasBits SCX -> sx += sIncr
                type hasBits SCY -> sy += sIncr
                else -> {
                    sx += sIncr
                    sy += sIncr
                }
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val savedComposite: Composite? = if (type hasBits AC && sx > 0.0 && sx < 1.0) {
            g2.composite.also {
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sx.toFloat())
            }
        } else {
            null
        }
        val path: GeneralPath? = if (type hasBits CLIP) GeneralPath() else null
        if (paint != null) {
            g2.paint = paint
        }
        for (txShape in txShapes) {
            if (path != null) {
                path.append(txShape, false)
            } else {
                g2.fill(txShape)
            }
        }
        if (path != null) {
            g2.clip(path)
        }
        if (savedComposite != null) {
            g2.composite = savedComposite
        }
    }

    @Suppress("unused")
    companion object
    {
        const val INC  =   1
        const val DEC  =   2
        const val R    =   4 // rotate
        const val SC   =   8 // scale
        const val SCX  =  16 // scale invert x
        const val SCY  =  32 // scale invert y
        const val AC   =  64 // AlphaComposite
        const val CLIP = 128 // Clipping
        const val NOP  = 512 // No Paint
        const val RI = R or INC
        const val RD = R or DEC
        const val SCI = SC or INC
        const val SCD = SC or DEC
        const val SCXI = SCX or SC or INC
        const val SCXD = SCX or SC or DEC
        const val SCYI = SCY or SC or INC
        const val SCYD = SCY or SC or DEC
    }
}
