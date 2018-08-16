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
package java2d.demos.Lines

import java2d.Surface
import java.awt.BasicStroke
import java.awt.Color.BLACK
import java.awt.Color.WHITE
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.font.TextLayout
import java.awt.geom.Arc2D
import java.awt.geom.CubicCurve2D
import java.awt.geom.Ellipse2D
import java.awt.geom.QuadCurve2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D

/**
 * Various shapes stroked with a dashing pattern.
 */
class Dash : Surface()
{
    init {
        background = WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val frc = g2.fontRenderContext
        val font = g2.font
        val tl = TextLayout("Dashes", font, frc)
        val sw = tl.bounds.width.toFloat()
        val sh = tl.ascent + tl.descent
        g2.color = BLACK
        tl.draw(g2, w / 2f - sw / 2, sh + 5)

        val dotted = BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f,
                                 floatArrayOf(0f, 6f, 0f, 6f), 0f)
        g2.stroke = dotted
        g2.drawRect(3, 3, w - 6, h - 6)

        var x = 0
        var y = h - 34

        val bs = arrayOfNulls<BasicStroke>(6)

        var j = 1.1f
        run {
            var i = 0
            while (i < bs.size) {
                val dash = floatArrayOf(j)
                val b = BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f)
                g2.stroke = b
                g2.drawLine(20, y, w - 20, y)
                bs[i] = BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f)
                y += 5
                i++
                j += 1.0f
            }
        }

        var shape: Shape? = null
        // y = 0;
        for (i in 0 .. 5) {
            x = if (i == 0 || i == 3) (w / 3 - w / 5) / 2 else x + w / 3
            y = if (i <= 2) sh.toInt() + h / 12 else h / 2

            g2.stroke = bs[i]
            g2.translate(x, y)
            when (i) {
                0 -> shape = Arc2D.Float(0.0f, 0.0f, w / 5f, h / 4f, 45f, 270f, Arc2D.PIE)
                1 -> shape = Ellipse2D.Float(0.0f, 0.0f, w / 5f, h / 4f)
                2 -> shape = RoundRectangle2D.Float(0.0f, 0.0f, w / 5f, h / 4f, 10.0f, 10.0f)
                3 -> shape = Rectangle2D.Float(0.0f, 0.0f, w / 5f, h / 4f)
                4 -> shape = QuadCurve2D.Float(0.0f, 0.0f, w / 10f, h / 2f, w / 5f, 0.0f)
                5 -> shape = CubicCurve2D.Float(0.0f, 0.0f, w / 15f, h / 2f, w / 10f, h / 4f, w / 5f, 0.0f)
            }

            g2.draw(shape)
            g2.translate(-x, -y)
        }
    }

    companion object
    {
        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Dash())
        }
    }
}
