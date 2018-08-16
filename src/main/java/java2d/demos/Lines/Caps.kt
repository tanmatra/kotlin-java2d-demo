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
import java.awt.Color
import java.awt.Graphics2D
import java.awt.font.TextLayout
import java.awt.geom.Line2D

/**
 * Shows the three different styles of stroke ending.
 */
class Caps : Surface()
{
    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val fontRenderContext = g2.fontRenderContext
        val font = g2.font
        g2.color = Color.BLACK
        for ((i, cap) in CAPS.withIndex()) {
            g2.stroke = BasicStroke(15f, cap, BasicStroke.JOIN_MITER)
            val y = (i + 1) * h / (CAPS.size + 1).toFloat()
            g2.draw(Line2D.Float(w / 4f, y, w - w / 4f, y))
            val tl = TextLayout(DESCRIPTIONS[i], font, fontRenderContext)
            tl.draw(g2, (w / 2 - tl.bounds.width / 2).toFloat(), y - 10)
        }
    }

    companion object
    {
        private val CAPS = intArrayOf(BasicStroke.CAP_BUTT, BasicStroke.CAP_ROUND, BasicStroke.CAP_SQUARE)
        private val DESCRIPTIONS = arrayOf("Butt Cap", "Round Cap", "Square Cap")

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(Caps())
        }
    }
}
