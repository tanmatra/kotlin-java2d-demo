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
import java.awt.Color.BLACK
import java.awt.Color.BLUE
import java.awt.Color.CYAN
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.MAGENTA
import java.awt.Color.ORANGE
import java.awt.Color.PINK
import java.awt.Color.RED
import java.awt.Color.WHITE
import java.awt.Color.YELLOW
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D

/**
 * Ellipse2D 25 animated expanding ellipses.
 */
class Ellipses : AnimatingSurface()
{
    private val ellipses = Array(25) { Ellipse2D.Float() }
    private val esize = DoubleArray(ellipses.size)
    private val estroke = FloatArray(ellipses.size)
    private var maxSize: Double = 0.0

    init {
        background = BLACK
        for (i in ellipses.indices) {
            getRandomXY(i, 20 * Math.random(), 200, 200)
        }
    }

    private fun getRandomXY(i: Int, size: Double, w: Int, h: Int) {
        esize[i] = size
        estroke[i] = 1.0f
        val x = Math.random() * (w - maxSize / 2)
        val y = Math.random() * (h - maxSize / 2)
        ellipses[i].setFrame(x, y, size, size)
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        maxSize = (newWidth / 10).toDouble()
        for (i in ellipses.indices) {
            getRandomXY(i, maxSize * Math.random(), newWidth, newHeight)
        }
    }

    override fun step(width: Int, height: Int) {
        for (i in ellipses.indices) {
            estroke[i] += 0.025f
            esize[i]++
            if (esize[i] > maxSize) {
                getRandomXY(i, 1.0, width, height)
            } else {
                ellipses[i].setFrame(ellipses[i].getX(), ellipses[i].getY(), esize[i], esize[i])
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        for (i in ellipses.indices) {
            g2.color = colors[i % colors.size]
            g2.stroke = BasicStroke(estroke[i])
            g2.draw(ellipses[i])
        }
    }

    companion object
    {
        private val colors = arrayOf(BLUE, CYAN, GREEN, MAGENTA, ORANGE, PINK, RED, YELLOW, LIGHT_GRAY, WHITE)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Ellipses())
        }
    }
}
