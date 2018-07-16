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
package java2d.demos.Arcs_Curves

import java2d.Surface
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.font.TextLayout
import java.awt.geom.CubicCurve2D
import java.awt.geom.Ellipse2D
import java.awt.geom.FlatteningPathIterator
import java.awt.geom.PathIterator.SEG_CUBICTO
import java.awt.geom.PathIterator.SEG_LINETO
import java.awt.geom.PathIterator.SEG_MOVETO
import java.awt.geom.PathIterator.SEG_QUADTO
import java.awt.geom.QuadCurve2D
import java.awt.geom.Rectangle2D

/**
 * CubicCurve2D & QuadCurve2D curves includes FlattenPathIterator example.
 */
class Curves : Surface()
{
    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.color = Color.BLACK
        val frc = g2.fontRenderContext
        var tl = TextLayout("QuadCurve2D", g2.font, frc)
        var xx = (w * .5 - tl.bounds.width / 2).toFloat()
        tl.draw(g2, xx, tl.ascent)

        tl = TextLayout("CubicCurve2D", g2.font, frc)
        xx = (w * .5 - tl.bounds.width / 2).toFloat()
        tl.draw(g2, xx, h * .5f)
        g2.stroke = BasicStroke(5.0f)

        var yy = 20f

        for (i in 0 .. 1) {
            for (j in 0 .. 2) {
                val shape: Shape = if (i == 0)
                    QuadCurve2D.Float(w * 0.1f, yy,
                                      w * 0.5f, 50f,
                                      w * 0.9f, yy)
                else CubicCurve2D.Float(w * 0.1f, yy,
                                        w * 0.4f, yy - 15,
                                        w * 0.6f, yy + 15,
                                        w * 0.9f, yy)
                g2.color = COLORS[j]
                if (j != 2) {
                    g2.draw(shape)
                }

                when (j) {
                    1 -> {
                        g2.color = Color.LIGHT_GRAY
                        val f = shape.getPathIterator(null)
                        while (!f.isDone) {
                            val pts = FloatArray(6)
                            when (f.currentSegment(pts)) {
                                SEG_MOVETO, SEG_LINETO -> g2.fill(Rectangle2D.Float(pts[0], pts[1], 5f, 5f))
                                SEG_CUBICTO, SEG_QUADTO -> {
                                    g2.fill(Rectangle2D.Float(pts[0], pts[1], 5f, 5f))
                                    if (pts[2] != 0f) {
                                        g2.fill(Rectangle2D.Float(pts[2], pts[3], 5f, 5f))
                                    }
                                    if (pts[4] != 0f) {
                                        g2.fill(Rectangle2D.Float(pts[4], pts[5], 5f, 5f))
                                    }
                                }
                            }
                            f.next()
                        }
                    }
                    2 -> {
                        val p = shape.getPathIterator(null)
                        val f = FlatteningPathIterator(p, 0.1)
                        while (!f.isDone) {
                            val pts = FloatArray(6)
                            when (f.currentSegment(pts)) {
                                SEG_MOVETO, SEG_LINETO -> g2.fill(Ellipse2D.Float(pts[0], pts[1], 3f, 3f))
                            }
                            f.next()
                        }
                    }
                }
                yy += (h / 6).toFloat()
            }
            yy = (h / 2 + 15).toFloat()
        }
    }

    companion object
    {
        private val COLORS = arrayOf(Color.BLUE, Color.GREEN, Color.RED)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Curves())
        }
    }
}
