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

import java2d.AnimatingSurface
import java.awt.BasicStroke
import java.awt.Color.BLACK
import java.awt.Color.GRAY
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.PINK
import java.awt.Color.WHITE
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

/**
 * Lines & Paths animation illustrating BasicStroke attributes.
 */
class LineAnim : AnimatingSurface()
{
    private val lines = arrayOfNulls<Line2D>(3)
    private val rAmt = IntArray(lines.size)
    private val direction = IntArray(lines.size)
    private val speed = IntArray(lines.size)
    private val strokes = arrayOfNulls<BasicStroke>(lines.size)
    private lateinit var path: GeneralPath
    private lateinit var points: Array<Point2D>
    private var size: Float = 0.toFloat()
    private val ellipse = Ellipse2D.Double()

    init {
        background = WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        size = if (newWidth > newHeight) newHeight / 6f else newWidth / 6f
        for (i in lines.indices) {
            lines[i] = Line2D.Float(0f, 0f, size, 0f)
            strokes[i] = BasicStroke(size / 3, caps[i], joins[i])
            rAmt[i] = i * 360 / lines.size
            direction[i] = i % 2
            speed[i] = i + 1
        }

        path = GeneralPath().apply {
            moveTo(size, -size / 2)
            lineTo(size + size / 2, 0f)
            lineTo(size, +size / 2)
        }

        ellipse.setFrame(
            newWidth / 2.0 - (size * 2).toDouble() - 4.5,
            newHeight / 2.0 - (size * 2).toDouble() - 4.5,
            (size * 4).toDouble(),
            (size * 4).toDouble())

        val pathIterator = ellipse.getPathIterator(null, 0.9)
        val pointsList = mutableListOf<Point2D>()
        while (!pathIterator.isDone) {
            val pt = FloatArray(6)
            when (pathIterator.currentSegment(pt)) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    pointsList += Point2D.Float(pt[0], pt[1])
                }
            }
            pathIterator.next()
        }
        points = pointsList.toTypedArray()
    }

    override fun step(width: Int, height: Int) {
        for (i in lines.indices) {
            if (direction[i] == CLOCKWISE) {
                rAmt[i] += speed[i]
                if (rAmt[i] == 360) {
                    rAmt[i] = 0
                }
            } else {
                rAmt[i] -= speed[i]
                if (rAmt[i] == 0) {
                    rAmt[i] = 360
                }
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        ellipse.setFrame(w / 2.0 - size, h / 2.0 - size, (size * 2).toDouble(), (size * 2).toDouble())
        g2.color = BLACK
        g2.draw(ellipse)

        for (i in lines.indices) {
            val at = AffineTransform.getTranslateInstance(w / 2.0, h / 2.0)
            at.rotate(Math.toRadians(rAmt[i].toDouble()))
            g2.stroke = strokes[i]
            g2.color = colors[i]
            g2.draw(at.createTransformedShape(lines[i]))
            g2.draw(at.createTransformedShape(path))

            var j = (rAmt[i].toDouble() / 360 * points.size).toInt()
            j = if (j == points.size) points.size - 1 else j
            ellipse.setFrame(points[j].x, points[j].y, 9.0, 9.0)
            g2.fill(ellipse)
        }

        g2.stroke = bs1
        g2.color = BLACK
        for (pt in points) {
            ellipse.setFrame(pt.x, pt.y, 9.0, 9.0)
            g2.draw(ellipse)
        }
    }

    companion object
    {
        private val caps = intArrayOf(BasicStroke.CAP_BUTT, BasicStroke.CAP_SQUARE, BasicStroke.CAP_ROUND)
        private val joins = intArrayOf(BasicStroke.JOIN_MITER, BasicStroke.JOIN_BEVEL, BasicStroke.JOIN_ROUND)
        private val colors = arrayOf(GRAY, PINK, LIGHT_GRAY)
        private val bs1 = BasicStroke(1.0f)
        private val CLOCKWISE = 0

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(LineAnim())
        }
    }
}
