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

import java2d.AnimatingSurface
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D

/**
 * Ellipse2D 25 animated expanding ellipses.
 */
class Ellipses : AnimatingSurface()
{
    private val ellipses = Array(25) { Ellipse2D.Float() }
    private val strokes = FloatArray(ellipses.size)
    private var maxSize: Double = 0.0

    init {
        background = Color.BLACK
        for (i in ellipses.indices) {
            randomizeEllipse(i, 20 * Math.random(), 200, 200)
        }
    }

    private fun randomizeEllipse(index: Int, size: Double, areaWidth: Int, areaHeight: Int) {
        strokes[index] = 1.0f
        val x = Math.random() * (areaWidth - maxSize / 2)
        val y = Math.random() * (areaHeight - maxSize / 2)
        ellipses[index].setFrame(x, y, size, size)
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        maxSize = (newWidth / 10).toDouble()
        for (i in ellipses.indices) {
            randomizeEllipse(i, maxSize * Math.random(), newWidth, newHeight)
        }
    }

    override fun step(width: Int, height: Int) {
        for ((i, ellipse) in ellipses.withIndex()) {
            strokes[i] += 0.025f
            ellipse.width += 1
            ellipse.height += 1
            if (ellipse.width > maxSize || ellipse.height > maxSize) {
                randomizeEllipse(i, 1.0, width, height)
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for ((i, ellipse) in ellipses.withIndex()) {
            g2.color = COLORS[i % COLORS.size]
            g2.stroke = BasicStroke(strokes[i])
            g2.draw(ellipse)
        }
    }

    companion object
    {
        private val COLORS = arrayOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK,
                                     Color.RED, Color.YELLOW, Color.LIGHT_GRAY, Color.WHITE)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Ellipses())
        }
    }
}
