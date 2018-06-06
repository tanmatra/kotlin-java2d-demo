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
import java2d.use
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform

/**
 * Transformation of characters.
 */
class Tree : AnimatingSurface()
{
    private var theC = 'A'
    private var theT: Char = theC
    private var theR: Char = (theC.toInt() + 1).toChar()

    init {
        background = Color.WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {}

    override fun step(width: Int, height: Int) {
        sleepAmount = 4000
        theC = (theC.toInt() + 1).toChar()
        theT = theC
        theR = (theC.toInt() + 1).toChar()
        if (theR == 'z') {
            theC = 'A'
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val mindim = Math.min(w, h)
        val at = AffineTransform().apply {
            translate((w - mindim) / 2.0, (h - mindim) / 2.0)
            scale(mindim.toDouble(), mindim.toDouble())
            translate(0.5, 0.5)
            scale(0.3, 0.3)
            translate(-(Twidth + Rwidth), FontHeight / 4.0)
        }
        g2.transform(at)
        tree(g2, mindim * 0.3, 0)
    }

    private fun tree(g2d: Graphics2D, size: Double, phase: Int) {
        var size1 = size
        g2d.color = colors[phase % 3]
        TextLayout(theT.toString(), theFont, g2d.fontRenderContext).draw(g2d, 0.0f, 0.0f)
        if (size1 > 10.0) {
            val at = AffineTransform()
            at.setToTranslation(Twidth, -0.1)
            at.scale(0.6, 0.6)
            g2d.transform(at)
            size1 *= 0.6
            TextLayout(theR.toString(), theFont, g2d.fontRenderContext).draw(g2d, 0.0f, 0.0f)
            at.setToTranslation(Rwidth + 0.75, 0.0)
            g2d.transform(at)
            (g2d.create() as Graphics2D).use { g2dt ->
                at.setToRotation(-Math.PI / 2.0)
                g2dt.transform(at)
                tree(g2dt, size1, phase + 1)
            }
            at.setToTranslation(.75, 0.0)
            at.rotate(-Math.PI / 2.0)
            at.scale(-1.0, 1.0)
            at.translate(-Twidth, 0.0)
            g2d.transform(at)
            tree(g2d, size1, phase)
        }
        g2d.transform = AffineTransform()
    }

    companion object
    {
        private var theFont = Font(Font.SERIF, Font.PLAIN, 1)
        private var Twidth = 0.6
        private var Rwidth = 0.6
        private var FontHeight = 0.75
        private var colors = arrayOf(Color.BLUE, Color.RED.darker(), Color.GREEN.darker())

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(Tree())
        }
    }
}
