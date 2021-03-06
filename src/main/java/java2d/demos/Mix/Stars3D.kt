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
package java2d.demos.Mix

import java2d.CControl
import java2d.ControlsSurface
import java2d.CustomControls
import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import javax.swing.JLabel
import javax.swing.JTextField

/**
 * Generate a 3D text shape with GeneralPath, render a number of small
 * multi-colored rectangles and then render the 3D text shape.
 */
class Stars3D : ControlsSurface()
{
    private var fontSize = 72
    private var text = "Java2D"

    init {
        background = Color.BLACK
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val rect = Rectangle2D.Double()
        repeat(STARS) { i ->
            g2.color = colors[i % 3]
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.random().toFloat())
            rect.setRect(w * Math.random(), h * Math.random(), 2.0, 2.0)
            g2.fill(rect)
        }

        val frc = g2.fontRenderContext
        val font = Font(Font.SERIF, Font.BOLD or Font.ITALIC, fontSize)
        val shape: Shape = font.createGlyphVector(frc, text).outline
        val transShape: Shape = transform.createTransformedShape(shape)
        val pathIterator = shape.getPathIterator(null)

        val seg = DoubleArray(6)
        val tseg = DoubleArray(6)

        val working = GeneralPath(Path2D.WIND_NON_ZERO)
        var x = 0.0
        var y = 0.0 // Current point on the path
        var tx = 0.0
        var ty = 0.0 // Transformed path point
        var cx = 0.0
        var cy = 0.0 // Last moveTo point, for SEG_CLOSE
        var tcx = 0.0
        var tcy = 0.0 // Transformed last moveTo point

        //
        // Iterate through the Shape and build the ribbon
        // by adding general path objects.
        //
        while (!pathIterator.isDone) {
            val segType = pathIterator.currentSegment(seg)
            when (segType) {
                PathIterator.SEG_MOVETO -> {
                    transform.transform(seg, 0, tseg, 0, 1)
                    x = seg[0]
                    y = seg[1]
                    tx = tseg[0]
                    ty = tseg[1]
                    cx = x
                    cy = y
                    tcx = tx
                    tcy = ty
                }
                PathIterator.SEG_LINETO -> {
                    transform.transform(seg, 0, tseg, 0, 1)
                    if (Line2D.relativeCCW(x, y, tx, ty, seg[0], seg[1]) < 0) {
                        working.moveTo(x, y)
                        working.lineTo(seg[0], seg[1])
                        working.lineTo(tseg[0], tseg[1])
                        working.lineTo(tx, ty)
                        working.lineTo(x, y)
                    } else {
                        working.moveTo(x, y)
                        working.lineTo(tx, ty)
                        working.lineTo(tseg[0], tseg[1])
                        working.lineTo(seg[0], seg[1])
                        working.lineTo(x, y)
                    }
                    x = seg[0]
                    y = seg[1]
                    tx = tseg[0]
                    ty = tseg[1]
                }
                PathIterator.SEG_QUADTO -> {
                    transform.transform(seg, 0, tseg, 0, 2)
                    if (Line2D.relativeCCW(x, y, tx, ty, seg[2], seg[3]) < 0) {
                        working.moveTo(x, y)
                        working.quadTo(seg[0], seg[1], seg[2], seg[3])
                        working.lineTo(tseg[2], tseg[3])
                        working.quadTo(tseg[0], tseg[1], tx, ty)
                        working.lineTo(x, y)
                    } else {
                        working.moveTo(x, y)
                        working.lineTo(tx, ty)
                        working.quadTo(tseg[0], tseg[1], tseg[2], tseg[3])
                        working.lineTo(seg[2], seg[3])
                        working.quadTo(seg[0], seg[1], x, y)
                    }
                    x = seg[2]
                    y = seg[3]
                    tx = tseg[2]
                    ty = tseg[3]
                }
                PathIterator.SEG_CUBICTO -> {
                    transform.transform(seg, 0, tseg, 0, 3)
                    if (Line2D.relativeCCW(x, y, tx, ty, seg[4], seg[5]) < 0) {
                        working.moveTo(x, y)
                        working.curveTo(seg[0], seg[1], seg[2], seg[3], seg[4], seg[5])
                        working.lineTo(tseg[4], tseg[5])
                        working.curveTo(tseg[2], tseg[3], tseg[0], tseg[1], tx, ty)
                        working.lineTo(x, y)
                    } else {
                        working.moveTo(x, y)
                        working.lineTo(tx, ty)
                        working.curveTo(tseg[0], tseg[1], tseg[2], tseg[3], tseg[4], tseg[5])
                        working.lineTo(seg[4], seg[5])
                        working.curveTo(seg[2], seg[3], seg[0], seg[1], x, y)
                    }
                    x = seg[4]
                    y = seg[5]
                    tx = tseg[4]
                    ty = tseg[5]
                }
                PathIterator.SEG_CLOSE -> {
                    if (Line2D.relativeCCW(x, y, tx, ty, cx, cy) < 0) {
                        working.moveTo(x, y)
                        working.lineTo(cx, cy)
                        working.lineTo(tcx, tcy)
                        working.lineTo(tx, ty)
                        working.lineTo(x, y)
                    } else {
                        working.moveTo(x, y)
                        working.lineTo(tx, ty)
                        working.lineTo(tcx, tcy)
                        working.lineTo(cx, cy)
                        working.lineTo(x, y)
                    }
                    x = cx
                    y = cy
                    tx = tcx
                    ty = tcy
                }
            }
            pathIterator.next()
        } // while
        val ribbon: Shape = working

        g2.composite = composite ?: AlphaComposite.SrcOver
        val r = shape.bounds
        g2.translate(w * 0.5 - r.width * 0.5, h * 0.5 + r.height * 0.5)

        g2.color = Color.BLUE
        g2.fill(transShape)
        g2.color = Color(255, 255, 255, 200)
        g2.fill(ribbon)

        g2.color = Color.WHITE
        g2.fill(shape)

        g2.color = Color.BLUE
        g2.draw(shape)
    }

    internal class DemoControls(private val demo: Stars3D) : CustomControls(demo.name)
    {
        private val textTextField = JTextField(demo.text).apply {
            preferredSize = Dimension(60, 20)
            addActionListener {
                demo.text = text.trim()
                demo.repaint()
            }
        }
        private val fontSizeTextField = JTextField(demo.fontSize.toString()).apply {
            preferredSize = Dimension(30, 20)
            addActionListener {
                val size = try {
                    text.trim().toInt()
                } catch (e: NumberFormatException) {
                    return@addActionListener
                }
                demo.fontSize = size.coerceAtLeast(10)
                demo.repaint()
            }
        }

        init {
            add(JLabel("  Text:"))
            add(textTextField)
            add(JLabel("  Size:"))
            add(fontSizeTextField)
        }

        override fun getPreferredSize() = Dimension(200, 37)

        override fun run() {
            val me = Thread.currentThread()
            try {
                Thread.sleep(999)
            } catch (e: Exception) {
                return
            }

            val length = size.width / 4
            val samples = arrayOf("JAVA" to length, "J2D" to length)
            while (thread === me) {
                for ((text, size) in samples) {
                    demo.fontSize = size
                    demo.text = text
                    fontSizeTextField.text = size.toString()
                    textTextField.text = text
                    demo.repaint()
                    try {
                        Thread.sleep(5555)
                    } catch (e: InterruptedException) {
                        return
                    }
                }
            }
            thread = null
        }
    }

    companion object
    {
        private const val STARS = 300

        private val colors = arrayOf(Color.RED, Color.GREEN, Color.WHITE)

        private val transform = AffineTransform.getTranslateInstance(-5.0, -5.0)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Stars3D())
        }
    }
}
