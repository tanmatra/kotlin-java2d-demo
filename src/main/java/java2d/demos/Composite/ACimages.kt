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
package java2d.demos.Composite

import java2d.Surface
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.font.TextLayout
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D

/**
 * Compositing shapes on images.
 */
class ACimages : Surface()
{
    private val images = IMAGE_NAMES.map { name -> getImage("$name.gif") }

    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        var alpha = 0.0f
        val iw = w / 3
        val ih = (h - 45) / 3
        var xx = 0.0f
        var yy = 15.0f

        for (i in images.indices) {
            xx = if (i % 3 == 0) 0.0f else xx + w / 3
            when (i) {
                3 -> yy = (h / 3 + 15).toFloat()
                6 -> yy = (h / 3 * 2 + 15).toFloat()
            }

            g2.composite = AlphaComposite.SrcOver
            g2.color = Color.BLACK
            alpha += 0.1f
            val ac = AlphaComposite.SrcOver.derive(alpha)
            val str = "a=%.1f".format(alpha)
            TextLayout(str, g2.font, g2.fontRenderContext).draw(g2, xx + 3, yy - 2)

            val shape: Shape = when (i % 3) {
                0 -> Ellipse2D.Float(xx, yy, iw.toFloat(), ih.toFloat())
                1 -> RoundRectangle2D.Float(xx, yy, iw.toFloat(), ih.toFloat(), 25f, 25f)
                2 -> Rectangle2D.Float(xx, yy, iw.toFloat(), ih.toFloat())
                else -> error(3)
            }
            g2.color = COLORS[i]
            g2.composite = ac
            g2.fill(shape)
            g2.drawImage(images[i], xx.toInt(), yy.toInt(), iw, ih, null)
        }
    }

    companion object
    {
        private val IMAGE_NAMES = arrayOf(
            "box", "fight", "magnify",
            "boxwave", "globe", "snooze",
            "tip", "thumbsup", "dukeplug")

        private val COLORS = arrayOf(
            Color.BLUE, Color.CYAN, Color.GREEN,
            Color.MAGENTA, Color.ORANGE, Color.PINK,
            Color.RED, Color.YELLOW, Color.LIGHT_GRAY)

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(ACimages())
        }
    }
}
