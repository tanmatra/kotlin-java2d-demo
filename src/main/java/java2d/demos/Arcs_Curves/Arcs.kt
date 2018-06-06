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
import java2d.Surface
import java.awt.BasicStroke
import java.awt.Color.BLACK
import java.awt.Color.BLUE
import java.awt.Color.GRAY
import java.awt.Color.WHITE
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D

/**
 * Arc2D Open, Chord & Pie arcs; Animated Pie Arc.
 */
class Arcs : AnimatingSurface()
{
    private var aw: Int = 0
    private var ah: Int = 0 // animated arc width & height
    private var tx: Int = 0
    private var ty: Int = 0
    private var angleStart = 45
    private var angleExtent = 270
    private var mouth = CLOSE
    private var direction = FORWARD

    init {
        background = WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        tx = 0
        ty = 0
        aw = newWidth / 12
        ah = newHeight / 12
    }

    override fun step(width: Int, height: Int) {
        // Compute direction
        when {
            tx + aw >= width - 5 && direction == FORWARD -> direction = DOWN
            ty + ah >= height - 5 && direction == DOWN -> direction = BACKWARD
            tx - aw <= 5 && direction == BACKWARD -> direction = UP
            ty - ah <= 5 && direction == UP -> direction = FORWARD
        }
        // compute angle start & extent
        when (mouth) {
            CLOSE -> {
                angleStart -= 5
                angleExtent += 10
            }
            OPEN -> {
                angleStart += 5
                angleExtent -= 10
            }
        }
        when (direction) {
            FORWARD -> {
                tx += 5
                ty = 0
            }
            DOWN -> {
                tx = width
                ty += 5
            }
            BACKWARD -> {
                tx -= 5
                ty = height
            }
            UP -> {
                tx = 0
                ty -= 5
            }
        }
        when {
            angleStart == 0 -> mouth = OPEN
            angleStart > 45 -> mouth = CLOSE
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        // Draw Arcs
        g2.stroke = BasicStroke(5.0f)
        for (i in types.indices) {
            val arc = Arc2D.Float(i)
            arc.setFrame(
                (i + 1).toDouble() * w.toDouble() * 0.2,
                (i + 1).toDouble() * h.toDouble() * 0.2,
                w * 0.17,
                h * 0.17)
            arc.angleStart = 45.0
            arc.angleExtent = 270.0
            g2.color = BLUE
            g2.draw(arc)
            g2.color = GRAY
            g2.fill(arc)
            g2.color = BLACK
            g2.drawString(types[i],
                          ((i + 1).toDouble() * w.toDouble() * .2).toInt(),
                          (((i + 1).toDouble() * h.toDouble() * .2) - 3).toInt())
        }

        // Draw Animated Pie Arc
        val pieArc = Arc2D.Float(Arc2D.PIE)
        pieArc.setFrame(0.0, 0.0, aw.toDouble(), ah.toDouble())
        pieArc.angleStart = angleStart.toDouble()
        pieArc.angleExtent = angleExtent.toDouble()
        val at = AffineTransform.getTranslateInstance(tx.toDouble(), ty.toDouble())
        when (direction) {
            DOWN -> at.rotate(Math.toRadians(90.0))
            BACKWARD -> at.rotate(Math.toRadians(180.0))
            UP -> at.rotate(Math.toRadians(270.0))
        }
        g2.color = BLUE
        g2.fill(at.createTransformedShape(pieArc))
    }

    companion object
    {
        private val types = arrayOf("Arc2D.OPEN", "Arc2D.CHORD", "Arc2D.PIE")
        private const val CLOSE = 0
        private const val OPEN = 1
        private const val FORWARD = 0
        private const val BACKWARD = 1
        private const val DOWN = 2
        private const val UP = 3

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(Arcs())
        }
    }
}
