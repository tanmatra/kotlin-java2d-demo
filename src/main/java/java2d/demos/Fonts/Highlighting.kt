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

import java2d.AnimatingSurface
import java2d.Surface
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.TextHitInfo
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

/**
 * Highlighting of text showing the caret, the highlight & the character
 * advances.
 */
class Highlighting : AnimatingSurface()
{
    private val curPos = IntArray(2)
    private val layouts: Array<TextLayout?> = arrayOfNulls(2)
    private lateinit var fonts: Array<Font>

    init {
        background = Color.WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        fonts = arrayOf(
            Font("Monospaced", Font.PLAIN, newWidth / texts[0].length + 8),
            Font("Serif", Font.BOLD, newWidth / texts[1].length))
        for (i in layouts.indices) {
            curPos[i] = 0
        }
    }

    override fun step(width: Int, height: Int) {
        sleepAmount = 900
        for (i in 0 .. 1) {
            layouts[i]?.let { textLayout ->
                if (curPos[i]++ == textLayout.characterCount) {
                    curPos[i] = 0
                }
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val frc = g2.fontRenderContext
        for (i in 0 .. 1) {
            val textLayout = TextLayout(texts[i], fonts[i], frc)
            layouts[i] = textLayout
            val rw: Float = textLayout.advance
            val rx: Float = (w - rw) / 2
            val ry: Float = if (i == 0) h / 3.0f else h * 0.75f

            // draw highlighted shape
            var hilite = textLayout.getLogicalHighlightShape(0, curPos[i])
            val at = AffineTransform.getTranslateInstance(rx.toDouble(), ry.toDouble())
            hilite = at.createTransformedShape(hilite)
            val hy = hilite.bounds2D.y.toFloat()
            val hh = hilite.bounds2D.height.toFloat()
            g2.color = colors[i]
            g2.fill(hilite)

            // get caret shape
            val shapes = textLayout.getCaretShapes(curPos[i])
            val caret = at.createTransformedShape(shapes[0])

            g2.color = Color.BLACK
            textLayout.draw(g2, rx, ry)
            g2.draw(caret)
            g2.draw(Rectangle2D.Float(rx, hy, rw, hh))

            // Display character advances.
            for (j in 0 .. textLayout.characterCount) {
                val cInfo = textLayout.getCaretInfo(TextHitInfo.leading(j))
                val str = cInfo[0].toInt().toString()
                val tl = TextLayout(str, smallFont, frc)
                tl.draw(g2, rx + cInfo[0] - tl.advance / 2, hy + hh + tl.ascent + 1.0f)
            }
        }
    }

    companion object
    {
        private val texts = arrayOf("HILIGHTING", "Java2D")
        private val colors = arrayOf(Color.CYAN, Color.LIGHT_GRAY)
        private val smallFont = Font(Font.MONOSPACED, Font.PLAIN, 8)

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(Highlighting())
        }
    }
}
