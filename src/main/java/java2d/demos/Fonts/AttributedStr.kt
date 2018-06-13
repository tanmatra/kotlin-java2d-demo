/*
 *
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java2d.demos.Fonts

import java2d.DemoFonts
import java2d.Surface
import java2d.rangeIndexOf
import java2d.shift
import java2d.use
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Paint
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.font.GraphicAttribute
import java.awt.font.ImageGraphicAttribute
import java.awt.font.LineBreakMeasurer
import java.awt.font.ShapeGraphicAttribute
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.text.AttributedCharacterIterator
import java.text.AttributedCharacterIterator.Attribute
import java.text.AttributedString

/**
 * Demonstrates how to build an AttributedString and then render the string broken over lines.
 */
class AttributedStr : Surface()
{
    private val aci: AttributedCharacterIterator

    init {
        background = Color.WHITE

        val font = DemoFonts.getFont("A.ttf").deriveFont(Font.PLAIN, 70f)
        val index = TEXT.indexOf("A") + 1
        attrStr.addAttribute(TextAttribute.FONT, font, 0, index)
        attrStr.addAttribute(TextAttribute.FOREGROUND, white, 0, index)

        val font2 = Font(Font.DIALOG, Font.PLAIN, 40)
        val size = getFontMetrics(font2).height
        val bi = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        bi.createGraphics().use { gfx ->
            gfx.drawImage(getImage("snooze.gif"), 0, 0, size, size, null)
        }
        val iga = ImageGraphicAttribute(bi, GraphicAttribute.TOP_ALIGNMENT)
        attrStr.addAttribute(TextAttribute.CHAR_REPLACEMENT, iga, TEXT.length - 1, TEXT.length)

        aci = attrStr.iterator
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        var x = 5f
        var y = 0f
        val frc = g2.fontRenderContext
        val lbm = LineBreakMeasurer(aci, frc)

        g2.paint = GradientPaint(0f, h.toFloat(), blue, w.toFloat(), 0f, black)
        g2.fillRect(0, 0, w, h)

        g2.color = white
        val s = "AttributedString LineBreakMeasurer"
        val font = Font(Font.SERIF, Font.PLAIN, 12)
        var tl = TextLayout(s, font, frc)

        y += tl.bounds.height.toFloat()
        tl.draw(g2, 5f, y)

        g2.color = yellow

        while (y < h - tl.ascent) {
            lbm.position = 0
            while (lbm.position < TEXT.length) {
                tl = lbm.nextLayout(w - x)
                if (!tl.isLeftToRight) {
                    x = w - tl.advance
                }
                y += tl.ascent
                tl.draw(g2, x, y)
                y += tl.descent + tl.leading
            }
        }
    }

    companion object
    {
        internal val black = Color(20, 20, 20)
        internal val blue = Color(94, 105, 176)
        internal val yellow = Color(255, 255, 140)
        internal val red = Color(149, 43, 42)
        internal val white = Color(240, 240, 255)
        internal const val TEXT = "  A quick brown  fox  jumped  over the lazy duke  "
        internal val attrStr = AttributedString(TEXT)

        init {
            val shape = Ellipse2D.Double(0.0, 25.0, 12.0, 12.0)
            val sga = ShapeGraphicAttribute(shape, GraphicAttribute.TOP_ALIGNMENT, false)
            attrStr.addAttribute(TextAttribute.CHAR_REPLACEMENT, sga, 0, 1)

            TEXT.rangeIndexOf("quick").let { range ->
                attrStr.setFont(range, Font(Font.SANS_SERIF, Font.BOLD or Font.ITALIC, 20))
            }

            TEXT.rangeIndexOf("brown").let { range ->
                attrStr.setForeground(range, red)
                attrStr.setFont(range, Font(Font.SERIF, Font.BOLD, 20))
            }

            TEXT.indexOf("fox").let { index ->
                val fontTransform = AffineTransform().apply { rotate(Math.toRadians(10.0)) }
                val fx = Font(Font.SERIF, Font.BOLD, 30).deriveFont(fontTransform)
                val range = index .. index
                attrStr.setFont(range, fx)
                attrStr.setFont(range shift 1, fx)
                attrStr.setFont(range shift 2, fx)
            }

            TEXT.rangeIndexOf("jumped").let { range ->
                val fontTransform = AffineTransform().apply { setToRotation(Math.toRadians(-4.0)) }
                val fx = Font(Font.SERIF, Font.BOLD, 20).deriveFont(fontTransform)
                attrStr.setFont(range, fx)
            }

            TEXT.rangeIndexOf("over").let { range ->
                attrStr.addAttribute(range, TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
                attrStr.setForeground(range, white)
                attrStr.setFont(range, Font(Font.SERIF, Font.BOLD or Font.ITALIC, 30))
            }

            TEXT.indexOf("over").let { index ->
                val end = TEXT.indexOf("duke")
                val range = index .. end
                attrStr.setFont(range, Font(Font.DIALOG, Font.PLAIN, 20))
            }

            TEXT.rangeIndexOf("duke").let { range ->
                val bufImg = BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB).apply {
                    setRGB(0, 0, 0xFFFFFFFF.toInt())
                }
                val texturePaint = TexturePaint(bufImg, Rectangle(0, 0, 4, 4))
                attrStr.setBackground(range, texturePaint)
                attrStr.setFont(range, Font(Font.SERIF, Font.BOLD, 40))
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            Surface.createDemoFrame(AttributedStr())
        }
    }
}

fun AttributedString.addAttribute(range: IntRange, attribute: Attribute, value: Any?) {
    addAttribute(attribute, value, range.start, range.endInclusive + 1)
}
fun AttributedString.setFont(range: IntRange, font: Font) {
    addAttribute(range, TextAttribute.FONT, font)
}
fun AttributedString.setForeground(range: IntRange, paint: Paint) {
    addAttribute(range, TextAttribute.FOREGROUND, paint)
}
fun AttributedString.setBackground(range: IntRange, paint: Paint) {
    addAttribute(range, TextAttribute.BACKGROUND, paint)
}
